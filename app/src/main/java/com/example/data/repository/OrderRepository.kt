package com.example.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.example.data.currency.CurrencyManager
import com.example.data.local.AppDatabase
import com.example.data.local.entities.ListingEntity
import com.example.data.local.entities.NotificationEntity
import com.example.data.local.entities.OrderEntity
import com.example.data.local.entities.SupportTicketEntity
import com.example.data.local.entities.UpiSubmissionEntity
import com.example.data.local.entities.UserEntity
import com.example.data.local.entities.WalletTransactionEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class OrderRepository(context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val orderDao = db.orderDao()
    private val listingDao = db.listingDao()
    private val deliveryVaultDao = db.deliveryVaultDao()
    private val notificationDao = db.notificationDao()
    private val userDao = db.userDao()
    private val walletDao = db.walletDao()
    private val upiSubmissionDao = db.upiSubmissionDao()
    private val ticketDao = db.supportTicketDao()

    fun getOrdersForBuyer(buyerId: String): Flow<List<OrderEntity>> = orderDao.observeOrdersForBuyer(buyerId)

    fun getOrdersForSeller(sellerId: String): Flow<List<OrderEntity>> = orderDao.observeOrdersForSeller(sellerId)

    fun observeAllOrdersForAdmin(): Flow<List<OrderEntity>> = orderDao.observeAllOrdersForAdmin()

    fun observeOrderById(id: String): Flow<OrderEntity?> = orderDao.observeOrderById(id)

    suspend fun getOrderById(id: String): OrderEntity? = orderDao.getOrderById(id)

    /**
     * ATOMIC WALLET CHECKOUT
     * 1. Validates authenticated buyer.
     * 2. Re-validates listing status == 'APPROVED' (prevents concurrent double purchase).
     * 3. Checks available wallet balance.
     * 4. Deducts exact payment atomically.
     * 5. Marks listing SOLD and releases delivery credentials.
     * 6. Writes immutable ledger records.
     */
    suspend fun purchaseWithWallet(buyer: UserEntity, listing: ListingEntity): Result<OrderEntity> {
        if (listing.isShowcase) {
            return Result.failure(IllegalStateException("Showcase accounts are presentation models and cannot be purchased."))
        }
        if (listing.sellerId == buyer.id) {
            return Result.failure(IllegalStateException("You cannot purchase your own account listing."))
        }

        return try {
            val order = db.withTransaction {
                // 1. Verify listing is still available
                val currentListing = listingDao.getListingById(listing.id)
                    ?: throw IllegalStateException("Listing no longer exists")
                if (currentListing.status != "APPROVED") {
                    throw IllegalStateException("This unique account has already been reserved or sold (status: ${currentListing.status})")
                }

                // 2. Verify buyer balance
                val currentBuyer = userDao.getUserById(buyer.id)
                    ?: throw IllegalStateException("Buyer account not found")

                val priceInBuyerCurrency = CurrencyManager.convert(
                    amount = currentListing.originalPrice,
                    fromCode = currentListing.originalCurrency,
                    toCode = currentBuyer.preferredCurrency
                )

                if (currentBuyer.walletBalance < priceInBuyerCurrency) {
                    throw IllegalStateException(
                        "Insufficient funds. Required: ${currentBuyer.preferredCurrency} ${String.format("%.2f", priceInBuyerCurrency)}, Available: ${currentBuyer.preferredCurrency} ${String.format("%.2f", currentBuyer.walletBalance)}"
                    )
                }

                // 3. Mark listing as SOLD immediately to prevent race conditions
                listingDao.updateStatus(currentListing.id, "SOLD")

                // 4. Deduct buyer wallet atomically
                val newBuyerBalance = currentBuyer.walletBalance - priceInBuyerCurrency
                userDao.updateBalances(currentBuyer.id, newBuyerBalance, currentBuyer.pendingBalance)

                // 5. Generate unique order number
                val orderNumber = "GB-${(2026..2030).random()}-${UUID.randomUUID().toString().take(6).uppercase()}"
                val orderId = "ORD-${UUID.randomUUID().toString().take(8).uppercase()}"

                val newOrder = OrderEntity(
                    id = orderId,
                    orderNumber = orderNumber,
                    listingId = currentListing.id,
                    listingTitle = currentListing.title,
                    gameTitle = currentListing.gameTitle,
                    buyerId = currentBuyer.id,
                    buyerEmail = currentBuyer.email,
                    sellerId = currentListing.sellerId,
                    sellerName = currentListing.sellerName,
                    amount = currentListing.originalPrice,
                    currency = currentListing.originalCurrency,
                    paymentMethod = "WALLET",
                    paymentStatus = "PAID",
                    deliveryStatus = "DELIVERED",
                    status = "Delivered",
                    transactionReference = "TXN-WAL-$orderNumber"
                )
                orderDao.insertOrder(newOrder)

                // 6. Record immutable debit in buyer ledger
                walletDao.insertTransaction(
                    WalletTransactionEntity(
                        id = "TXN-DEB-${UUID.randomUUID().toString().take(8).uppercase()}",
                        userId = currentBuyer.id,
                        type = "PAYMENT_DEBIT",
                        amount = priceInBuyerCurrency,
                        currency = currentBuyer.preferredCurrency,
                        status = "COMPLETED",
                        referenceId = orderNumber,
                        description = "Payment for ${currentListing.title} ($orderNumber)",
                        balanceAfter = newBuyerBalance,
                        createdAt = System.currentTimeMillis()
                    )
                )

                // 7. Credit seller wallet with sale proceeds
                val seller = userDao.getUserById(currentListing.sellerId)
                if (seller != null) {
                    val priceInSellerCurrency = CurrencyManager.convert(
                        amount = currentListing.originalPrice,
                        fromCode = currentListing.originalCurrency,
                        toCode = seller.preferredCurrency
                    )
                    val newSellerBalance = seller.walletBalance + priceInSellerCurrency
                    userDao.updateBalances(seller.id, newSellerBalance, seller.pendingBalance)

                    walletDao.insertTransaction(
                        WalletTransactionEntity(
                            id = "TXN-CRE-${UUID.randomUUID().toString().take(8).uppercase()}",
                            userId = seller.id,
                            type = "SALE_CREDIT",
                            amount = priceInSellerCurrency,
                            currency = seller.preferredCurrency,
                            status = "COMPLETED",
                            referenceId = orderNumber,
                            description = "Escrow Payout: Sale of ${currentListing.title} ($orderNumber)",
                            balanceAfter = newSellerBalance,
                            createdAt = System.currentTimeMillis()
                        )
                    )
                }

                // 8. Release delivery vault to verified buyer
                deliveryVaultDao.markReleased(currentListing.id, currentBuyer.id)

                // 9. Send notifications
                notificationDao.insertNotification(
                    NotificationEntity(
                        id = "notif_${UUID.randomUUID().toString().take(6)}",
                        userId = currentBuyer.id,
                        title = "Order $orderNumber Complete!",
                        message = "Your purchase of '${currentListing.title}' is confirmed. Decrypted credentials are now unlocked in My Purchases.",
                        type = "ORDER_STATUS"
                    )
                )

                notificationDao.insertNotification(
                    NotificationEntity(
                        id = "notif_${UUID.randomUUID().toString().take(6)}",
                        userId = currentListing.sellerId,
                        title = "Account Sold! ($orderNumber)",
                        message = "Your listing '${currentListing.title}' was purchased. Earnings have been credited to your wallet balance.",
                        type = "ORDER_STATUS"
                    )
                )

                newOrder
            }
            Result.success(order)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * UPI PAYMENT ORDER CREATION
     * 1. Validates listing availability and reserves it (status = 'PENDING').
     * 2. Sets Order status = 'Payment Under Review'.
     * 3. Sends submission to admin for authorized verification.
     */
    suspend fun purchaseWithUpi(
        buyer: UserEntity,
        listing: ListingEntity,
        utrNumber: String,
        screenshotUrl: String,
        merchantUpiId: String = "gamerbazaar.escrow@icici"
    ): Result<OrderEntity> {
        if (utrNumber.trim().length < 6) {
            return Result.failure(IllegalArgumentException("Please enter a valid 12-digit UPI UTR or bank reference number"))
        }

        return try {
            val order = db.withTransaction {
                val currentListing = listingDao.getListingById(listing.id)
                    ?: throw IllegalStateException("Listing no longer exists")
                if (currentListing.status != "APPROVED") {
                    throw IllegalStateException("This unique account is not available for purchase (status: ${currentListing.status})")
                }

                // Reserve listing so other buyers cannot checkout concurrently
                listingDao.updateStatus(currentListing.id, "PENDING")

                val orderNumber = "GB-UPI-${(2026..2030).random()}-${UUID.randomUUID().toString().take(6).uppercase()}"
                val orderId = "ORD-${UUID.randomUUID().toString().take(8).uppercase()}"
                val inrAmount = CurrencyManager.convert(currentListing.originalPrice, currentListing.originalCurrency, "INR")

                val newOrder = OrderEntity(
                    id = orderId,
                    orderNumber = orderNumber,
                    listingId = currentListing.id,
                    listingTitle = currentListing.title,
                    gameTitle = currentListing.gameTitle,
                    buyerId = buyer.id,
                    buyerEmail = buyer.email,
                    sellerId = currentListing.sellerId,
                    sellerName = currentListing.sellerName,
                    amount = currentListing.originalPrice,
                    currency = currentListing.originalCurrency,
                    paymentMethod = "UPI",
                    paymentStatus = "UNDER_REVIEW",
                    deliveryStatus = "PENDING",
                    status = "Payment Under Review",
                    transactionReference = "UPI-REF-$orderNumber",
                    utrNumber = utrNumber.trim(),
                    screenshotUrl = screenshotUrl.trim()
                )
                orderDao.insertOrder(newOrder)

                // Insert UPI payment submission record for admin audit
                upiSubmissionDao.insertSubmission(
                    UpiSubmissionEntity(
                        id = "UPI-SUB-${UUID.randomUUID().toString().take(6).uppercase()}",
                        orderId = orderId,
                        userId = buyer.id,
                        userEmail = buyer.email,
                        amountInr = inrAmount,
                        utrNumber = utrNumber.trim(),
                        merchantUpiId = merchantUpiId,
                        screenshotUrl = screenshotUrl.trim(),
                        status = "UNDER_REVIEW",
                        createdAt = System.currentTimeMillis()
                    )
                )

                notificationDao.insertNotification(
                    NotificationEntity(
                        id = "notif_${UUID.randomUUID().toString().take(6)}",
                        userId = buyer.id,
                        title = "Payment Under Review ($orderNumber)",
                        message = "Your payment of ₹${String.format("%.2f", inrAmount)} (UTR: ${utrNumber.trim()}) is being audited by the merchant team.",
                        type = "ORDER_STATUS"
                    )
                )

                newOrder
            }
            Result.success(order)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * CRYPTO PAYMENTS
     * Displays coin, network, amount, and reference. Validates payment provider configuration.
     */
    suspend fun purchaseWithCrypto(
        buyer: UserEntity,
        listing: ListingEntity,
        coin: String,
        network: String,
        isProviderConfigured: Boolean
    ): Result<OrderEntity> {
        if (!isProviderConfigured) {
            return Result.failure(
                IllegalStateException("Crypto Gateway: Provider API credentials are not yet configured on this server. Please use Wallet or UPI.")
            )
        }

        return try {
            val order = db.withTransaction {
                val currentListing = listingDao.getListingById(listing.id)
                    ?: throw IllegalStateException("Listing not found")
                if (currentListing.status != "APPROVED") {
                    throw IllegalStateException("Account already reserved or sold")
                }

                // Reserve listing
                listingDao.updateStatus(currentListing.id, "PENDING")

                val orderNumber = "GB-CRYPTO-${UUID.randomUUID().toString().take(6).uppercase()}"
                val orderId = "ORD-${UUID.randomUUID().toString().take(8).uppercase()}"

                val newOrder = OrderEntity(
                    id = orderId,
                    orderNumber = orderNumber,
                    listingId = currentListing.id,
                    listingTitle = currentListing.title,
                    gameTitle = currentListing.gameTitle,
                    buyerId = buyer.id,
                    buyerEmail = buyer.email,
                    sellerId = currentListing.sellerId,
                    sellerName = currentListing.sellerName,
                    amount = currentListing.originalPrice,
                    currency = currentListing.originalCurrency,
                    paymentMethod = "CRYPTO ($coin-$network)",
                    paymentStatus = "PENDING",
                    deliveryStatus = "PENDING",
                    status = "Pending Payment",
                    transactionReference = "CRYPTO-TX-${UUID.randomUUID().toString().take(8).uppercase()}"
                )
                orderDao.insertOrder(newOrder)

                newOrder
            }
            Result.success(order)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Admin approves verified payment for an order.
     * Guaranteed exact-once execution: validates state before releasing credentials.
     */
    suspend fun adminApproveOrderPayment(orderId: String, adminId: String): Result<Unit> {
        return try {
            db.withTransaction {
                val order = orderDao.getOrderById(orderId)
                    ?: throw IllegalArgumentException("Order not found")
                if (order.status != "Payment Under Review" && order.paymentStatus != "UNDER_REVIEW") {
                    throw IllegalStateException("Order is not under review (current status: ${order.status})")
                }

                // 1. Mark order Delivered
                orderDao.updateStatusFields(
                    id = order.id,
                    status = "Delivered",
                    paymentStatus = "PAID",
                    deliveryStatus = "DELIVERED"
                )

                // 2. Mark listing as SOLD permanently
                listingDao.updateStatus(order.listingId, "SOLD")

                // 3. Release delivery vault to buyer
                deliveryVaultDao.markReleased(order.listingId, order.buyerId)

                // 4. Credit seller wallet with sale proceeds
                val seller = userDao.getUserById(order.sellerId)
                if (seller != null) {
                    val priceInSellerCurrency = CurrencyManager.convert(order.amount, order.currency, seller.preferredCurrency)
                    val newBalance = seller.walletBalance + priceInSellerCurrency
                    userDao.updateBalances(seller.id, newBalance, seller.pendingBalance)

                    walletDao.insertTransaction(
                        WalletTransactionEntity(
                            id = "TXN-SALE-${UUID.randomUUID().toString().take(8).uppercase()}",
                            userId = seller.id,
                            type = "SALE_CREDIT",
                            amount = priceInSellerCurrency,
                            currency = seller.preferredCurrency,
                            status = "COMPLETED",
                            referenceId = order.orderNumber,
                            description = "UPI Verified Payout: ${order.listingTitle} (${order.orderNumber})",
                            balanceAfter = newBalance,
                            createdAt = System.currentTimeMillis()
                        )
                    )
                }

                // 5. Update submission record if present
                val submission = upiSubmissionDao.getSubmissionByOrderId(orderId)
                if (submission != null) {
                    upiSubmissionDao.updateSubmission(
                        submission.copy(
                            status = "APPROVED",
                            reviewedByAdminId = adminId,
                            reviewedAt = System.currentTimeMillis()
                        )
                    )
                }

                // 6. Notify buyer and seller
                notificationDao.insertNotification(
                    NotificationEntity(
                        id = "notif_${UUID.randomUUID().toString().take(6)}",
                        userId = order.buyerId,
                        title = "Payment Approved! (${order.orderNumber})",
                        message = "Your payment for '${order.listingTitle}' has been verified. Account credentials are now unlocked in My Purchases.",
                        type = "PAYMENT_APPROVED"
                    )
                )

                db.adminAuditDao().insertAuditLog(
                    com.example.data.local.entities.AdminAuditLogEntity(
                        id = "LOG-PAY-${System.currentTimeMillis()}",
                        adminId = adminId,
                        adminName = "Admin",
                        action = "APPROVE_UPI_PAYMENT",
                        targetType = "ORDER",
                        targetId = orderId,
                        details = "Payment approved for order ${order.orderNumber} ($orderId)"
                    )
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Admin rejects unverified payment for an order.
     * Restores listing availability back to 'APPROVED'.
     */
    suspend fun adminRejectOrderPayment(orderId: String, adminId: String, reason: String): Result<Unit> {
        if (reason.isBlank()) {
            return Result.failure(IllegalArgumentException("Rejection reason is mandatory"))
        }

        return try {
            db.withTransaction {
                val order = orderDao.getOrderById(orderId)
                    ?: throw IllegalArgumentException("Order not found")

                // Update order to Rejected / Failed
                orderDao.updateStatusFields(
                    id = order.id,
                    status = "Pending Payment",
                    paymentStatus = "FAILED",
                    deliveryStatus = "PENDING"
                )

                // Restore listing status back to APPROVED so genuine buyers can purchase it
                listingDao.updateStatus(order.listingId, "APPROVED")

                // Update submission record
                val submission = upiSubmissionDao.getSubmissionByOrderId(orderId)
                if (submission != null) {
                    upiSubmissionDao.updateSubmission(
                        submission.copy(
                            status = "REJECTED",
                            rejectionReason = reason,
                            reviewedByAdminId = adminId,
                            reviewedAt = System.currentTimeMillis()
                        )
                    )
                }

                notificationDao.insertNotification(
                    NotificationEntity(
                        id = "notif_${UUID.randomUUID().toString().take(6)}",
                        userId = order.buyerId,
                        title = "Payment Not Verified (${order.orderNumber})",
                        message = "Your payment submission was rejected: $reason. The reservation has been released.",
                        type = "PAYMENT_REJECTED"
                    )
                )

                db.adminAuditDao().insertAuditLog(
                    com.example.data.local.entities.AdminAuditLogEntity(
                        id = "LOG-PAY-${System.currentTimeMillis()}",
                        adminId = adminId,
                        adminName = "Admin",
                        action = "REJECT_UPI_PAYMENT",
                        targetType = "ORDER",
                        targetId = orderId,
                        details = "Payment rejected for order ${order.orderNumber}. Reason: $reason"
                    )
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Open a dispute on an order.
     */
    suspend fun openDispute(orderId: String, userId: String, reason: String): Result<Unit> {
        if (reason.isBlank()) {
            return Result.failure(IllegalArgumentException("Please state the reason for the dispute"))
        }

        val order = orderDao.getOrderById(orderId)
            ?: return Result.failure(IllegalArgumentException("Order not found"))

        if (order.buyerId != userId) {
            return Result.failure(SecurityException("Unauthorized: Only the buyer can file a dispute for this order"))
        }

        db.withTransaction {
            val updatedOrder = order.copy(
                status = "Disputed",
                disputeStatus = "OPEN",
                disputeReason = reason.trim(),
                updatedAt = System.currentTimeMillis()
            )
            orderDao.updateOrder(updatedOrder)

            // Auto-create linked Support Ticket
            ticketDao.insertTicket(
                SupportTicketEntity(
                    id = "TKT-DISP-${UUID.randomUUID().toString().take(6).uppercase()}",
                    userId = order.buyerId,
                    userEmail = order.buyerEmail,
                    orderId = order.id,
                    listingTitle = order.listingTitle,
                    subject = "Dispute: ${order.orderNumber} - ${order.listingTitle}",
                    message = reason.trim(),
                    category = "DISPUTE",
                    status = "OPEN",
                    adminResponse = ""
                )
            )

            notificationDao.insertNotification(
                NotificationEntity(
                    id = "notif_${UUID.randomUUID().toString().take(6)}",
                    userId = order.buyerId,
                    title = "Dispute Opened (${order.orderNumber})",
                    message = "Your dispute has been logged with Gamer Bazaar Escrow arbitration.",
                    type = "DISPUTE_UPDATE"
                )
            )
        }

        return Result.success(Unit)
    }

    /**
     * Admin arbitrates a disputed order.
     * If favorBuyer is true: calls processRefund to atomically credit buyer wallet.
     * If favorBuyer is false: marks dispute as RESOLVED and keeps order Delivered.
     */
    suspend fun arbitrateDispute(
        orderId: String,
        adminId: String,
        favorBuyer: Boolean,
        reason: String
    ): Result<Unit> {
        return if (favorBuyer) {
            processRefund(orderId, adminId, reason)
        } else {
            try {
                db.withTransaction {
                    val order = orderDao.getOrderById(orderId)
                        ?: throw IllegalArgumentException("Order not found")
                    val updated = order.copy(
                        status = "Delivered",
                        disputeStatus = "RESOLVED",
                        rejectionReason = "Dispute resolved in favor of seller: $reason",
                        updatedAt = System.currentTimeMillis()
                    )
                    orderDao.updateOrder(updated)

                    notificationDao.insertNotification(
                        NotificationEntity(
                            id = "notif_${UUID.randomUUID().toString().take(6)}",
                            userId = order.buyerId,
                            title = "Dispute Resolved (${order.orderNumber})",
                            message = "Escrow arbitration resolved this dispute: $reason",
                            type = "DISPUTE_UPDATE"
                        )
                    )

                    db.adminAuditDao().insertAuditLog(
                        com.example.data.local.entities.AdminAuditLogEntity(
                            id = "LOG-DISP-${System.currentTimeMillis()}",
                            adminId = adminId,
                            adminName = "Admin",
                            action = "RESOLVE_DISPUTE_RELEASE",
                            targetType = "ORDER",
                            targetId = orderId,
                            details = "Dispute on order ${order.orderNumber} resolved in favor of seller: $reason"
                        )
                    )
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Admin processes an approved refund for an order.
     * Atomically credits buyer wallet and updates order status.
     */
    suspend fun processRefund(orderId: String, adminId: String, reason: String): Result<Unit> {
        return try {
            db.withTransaction {
                val order = orderDao.getOrderById(orderId)
                    ?: throw IllegalArgumentException("Order not found")

                if (order.status == "Refunded") {
                    throw IllegalStateException("Order is already refunded")
                }

                val buyer = userDao.getUserById(order.buyerId)
                    ?: throw IllegalStateException("Buyer user not found")

                val refundAmountInBuyerCurrency = CurrencyManager.convert(
                    amount = order.amount,
                    fromCode = order.currency,
                    toCode = buyer.preferredCurrency
                )

                // 1. Credit buyer balance
                val newBuyerBalance = buyer.walletBalance + refundAmountInBuyerCurrency
                userDao.updateBalances(buyer.id, newBuyerBalance, buyer.pendingBalance)

                // 2. Record immutable refund in wallet ledger
                walletDao.insertTransaction(
                    WalletTransactionEntity(
                        id = "TXN-REF-${UUID.randomUUID().toString().take(8).uppercase()}",
                        userId = buyer.id,
                        type = "REFUND_CREDIT",
                        amount = refundAmountInBuyerCurrency,
                        currency = buyer.preferredCurrency,
                        status = "COMPLETED",
                        referenceId = order.orderNumber,
                        description = "Refund for ${order.orderNumber}: $reason",
                        balanceAfter = newBuyerBalance,
                        createdAt = System.currentTimeMillis()
                    )
                )

                // 3. Mark order as Refunded
                val updatedOrder = order.copy(
                    status = "Refunded",
                    paymentStatus = "REFUNDED",
                    deliveryStatus = "PENDING",
                    disputeStatus = "RESOLVED",
                    rejectionReason = "Refunded: $reason",
                    updatedAt = System.currentTimeMillis()
                )
                orderDao.updateOrder(updatedOrder)

                // 4. Restore listing to APPROVED or keep CLOSED
                listingDao.updateStatus(order.listingId, "APPROVED")

                notificationDao.insertNotification(
                    NotificationEntity(
                        id = "notif_${UUID.randomUUID().toString().take(6)}",
                        userId = order.buyerId,
                        title = "Refund Processed (${order.orderNumber})",
                        message = "Your refund of ${buyer.preferredCurrency} ${String.format("%.2f", refundAmountInBuyerCurrency)} has been credited to your wallet balance.",
                        type = "WALLET_CREDIT"
                    )
                )

                db.adminAuditDao().insertAuditLog(
                    com.example.data.local.entities.AdminAuditLogEntity(
                        id = "LOG-REF-${System.currentTimeMillis()}",
                        adminId = adminId,
                        adminName = "Admin",
                        action = "RESOLVE_DISPUTE_REFUND",
                        targetType = "ORDER",
                        targetId = orderId,
                        details = "Refund processed for order ${order.orderNumber}: $reason. Buyer wallet credited with ${buyer.preferredCurrency} ${String.format("%.2f", refundAmountInBuyerCurrency)}."
                    )
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
