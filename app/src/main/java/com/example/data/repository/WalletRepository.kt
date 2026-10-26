package com.example.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.example.data.currency.CurrencyManager
import com.example.data.local.AppDatabase
import com.example.data.local.entities.NotificationEntity
import com.example.data.local.entities.UpiSubmissionEntity
import com.example.data.local.entities.WalletTransactionEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class WalletRepository(context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val userDao = db.userDao()
    private val walletDao = db.walletDao()
    private val upiSubmissionDao = db.upiSubmissionDao()
    private val notificationDao = db.notificationDao()

    fun observeTransactions(userId: String): Flow<List<WalletTransactionEntity>> {
        return walletDao.observeTransactionsForUser(userId)
    }

    fun observeUpiSubmissionsForAdmin(): Flow<List<UpiSubmissionEntity>> {
        return upiSubmissionDao.observePendingSubmissions()
    }

    /**
     * Submit manual UPI payment for Admin review.
     * Crucial Security Rule: Funds are NOT credited immediately on user input or screenshot.
     * They are recorded as UNDER_REVIEW in the ledger and credited only after authorized admin confirmation.
     */
    suspend fun submitUpiDeposit(
        userId: String,
        amountInr: Double,
        utrNumber: String,
        screenshotUrl: String,
        merchantUpiId: String = "gamerbazaar.escrow@icici"
    ): Result<UpiSubmissionEntity> {
        val user = userDao.getUserById(userId)
            ?: return Result.failure(IllegalStateException("User not found"))

        if (amountInr <= 0.0) {
            return Result.failure(IllegalArgumentException("Deposit amount must be greater than zero"))
        }
        if (utrNumber.trim().length < 6) {
            return Result.failure(IllegalArgumentException("Please enter a valid 12-digit UPI UTR or bank reference number"))
        }

        val submissionId = "UPI-SUB-${UUID.randomUUID().toString().take(6).uppercase()}"
        val submission = UpiSubmissionEntity(
            id = submissionId,
            orderId = "WALLET_TOPUP",
            userId = user.id,
            userEmail = user.email,
            amountInr = amountInr,
            utrNumber = utrNumber.trim(),
            merchantUpiId = merchantUpiId,
            screenshotUrl = screenshotUrl.trim(),
            status = "UNDER_REVIEW",
            rejectionReason = "",
            createdAt = System.currentTimeMillis()
        )

        // Record pending transaction in wallet ledger
        val txn = WalletTransactionEntity(
            id = "TXN-DEP-${UUID.randomUUID().toString().take(8).uppercase()}",
            userId = user.id,
            type = "DEPOSIT_UPI",
            amount = amountInr,
            currency = "INR",
            status = "UNDER_REVIEW",
            referenceId = utrNumber.trim(),
            description = "UPI Top-up (UTR: ${utrNumber.trim()}) - Pending Verification",
            balanceAfter = user.walletBalance,
            createdAt = System.currentTimeMillis()
        )

        db.withTransaction {
            upiSubmissionDao.insertSubmission(submission)
            walletDao.insertTransaction(txn)
            // Add to pending balance for transparency
            userDao.updateBalances(
                userId = user.id,
                available = user.walletBalance,
                pending = user.pendingBalance + amountInr
            )
        }

        notificationDao.insertNotification(
            NotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(6)}",
                userId = user.id,
                title = "UPI Payment Submitted",
                message = "Your deposit of ₹$amountInr (UTR: $utrNumber) is under merchant verification.",
                type = "ORDER_STATUS"
            )
        )

        return Result.success(submission)
    }

    /**
     * Admin approves verified UPI payment.
     * Executed exactly once. Prevents double credits.
     */
    suspend fun approveUpiDeposit(
        submissionId: String,
        adminId: String
    ): Result<Unit> {
        val sub = upiSubmissionDao.getSubmissionById(submissionId)
            ?: return Result.failure(IllegalArgumentException("Submission not found"))

        if (sub.status != "UNDER_REVIEW") {
            return Result.failure(IllegalStateException("Payment already processed (status: ${sub.status})"))
        }

        val user = userDao.getUserById(sub.userId)
            ?: return Result.failure(IllegalStateException("User not found"))

        // Convert INR amount to user preferred currency if different
        val creditAmount = CurrencyManager.convert(sub.amountInr, "INR", user.preferredCurrency)
        val newAvailable = user.walletBalance + creditAmount
        val newPending = (user.pendingBalance - sub.amountInr).coerceAtLeast(0.0)

        val txn = WalletTransactionEntity(
            id = "TXN-APR-${UUID.randomUUID().toString().take(8).uppercase()}",
            userId = user.id,
            type = "DEPOSIT_UPI",
            amount = creditAmount,
            currency = user.preferredCurrency,
            status = "COMPLETED",
            referenceId = sub.utrNumber,
            description = "UPI Deposit Verified (₹${sub.amountInr} -> $creditAmount ${user.preferredCurrency})",
            balanceAfter = newAvailable,
            createdAt = System.currentTimeMillis()
        )

        db.withTransaction {
            upiSubmissionDao.updateSubmission(
                sub.copy(
                    status = "APPROVED",
                    reviewedByAdminId = adminId,
                    reviewedAt = System.currentTimeMillis()
                )
            )
            userDao.updateBalances(user.id, newAvailable, newPending)
            walletDao.insertTransaction(txn)
        }

        notificationDao.insertNotification(
            NotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(6)}",
                userId = user.id,
                title = "Payment Approved!",
                message = "Your deposit of ₹${sub.amountInr} has been verified and added to your available wallet balance.",
                type = "PAYMENT_APPROVED"
            )
        )

        db.adminAuditDao().insertAuditLog(
            com.example.data.local.entities.AdminAuditLogEntity(
                id = "LOG-WAL-${System.currentTimeMillis()}",
                adminId = adminId,
                adminName = "Admin",
                action = "APPROVE_UPI_PAYMENT",
                targetType = "PAYMENT",
                targetId = submissionId,
                details = "Approved UPI deposit for user ${user.id}: ₹${sub.amountInr} credited as $creditAmount ${user.preferredCurrency} (UTR: ${sub.utrNumber})"
            )
        )

        return Result.success(Unit)
    }

    /**
     * Admin rejects unverified UPI submission with required reason.
     */
    suspend fun rejectUpiDeposit(
        submissionId: String,
        adminId: String,
        rejectionReason: String
    ): Result<Unit> {
        if (rejectionReason.isBlank()) {
            return Result.failure(IllegalArgumentException("A rejection reason is required"))
        }

        val sub = upiSubmissionDao.getSubmissionById(submissionId)
            ?: return Result.failure(IllegalArgumentException("Submission not found"))

        if (sub.status != "UNDER_REVIEW") {
            return Result.failure(IllegalStateException("Payment already processed"))
        }

        val user = userDao.getUserById(sub.userId)
            ?: return Result.failure(IllegalStateException("User not found"))

        val newPending = (user.pendingBalance - sub.amountInr).coerceAtLeast(0.0)

        val txn = WalletTransactionEntity(
            id = "TXN-REJ-${UUID.randomUUID().toString().take(8).uppercase()}",
            userId = user.id,
            type = "DEPOSIT_UPI",
            amount = sub.amountInr,
            currency = "INR",
            status = "REJECTED",
            referenceId = sub.utrNumber,
            description = "Deposit Rejected: $rejectionReason",
            balanceAfter = user.walletBalance,
            createdAt = System.currentTimeMillis()
        )

        db.withTransaction {
            upiSubmissionDao.updateSubmission(
                sub.copy(
                    status = "REJECTED",
                    rejectionReason = rejectionReason,
                    reviewedByAdminId = adminId,
                    reviewedAt = System.currentTimeMillis()
                )
            )
            userDao.updateBalances(user.id, user.walletBalance, newPending)
            walletDao.insertTransaction(txn)
        }

        notificationDao.insertNotification(
            NotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(6)}",
                userId = user.id,
                title = "Payment Rejected",
                message = "Your deposit of ₹${sub.amountInr} was rejected: $rejectionReason",
                type = "PAYMENT_REJECTED"
            )
        )

        db.adminAuditDao().insertAuditLog(
            com.example.data.local.entities.AdminAuditLogEntity(
                id = "LOG-WAL-${System.currentTimeMillis()}",
                adminId = adminId,
                adminName = "Admin",
                action = "REJECT_UPI_PAYMENT",
                targetType = "PAYMENT",
                targetId = submissionId,
                details = "Rejected UPI deposit for user ${user.id}: ₹${sub.amountInr}. Reason: $rejectionReason"
            )
        )

        return Result.success(Unit)
    }

    fun observeAllTransactionsForAdmin(): Flow<List<WalletTransactionEntity>> = walletDao.observeAllTransactionsForAdmin()


    /**
     * Process validated withdrawal where supported.
     */
    suspend fun withdrawFunds(
        userId: String,
        amount: Double,
        destinationDetails: String
    ): Result<WalletTransactionEntity> {
        val user = userDao.getUserById(userId)
            ?: return Result.failure(IllegalStateException("User not found"))

        if (amount <= 0.0) {
            return Result.failure(IllegalArgumentException("Amount must be greater than zero"))
        }
        if (user.walletBalance < amount) {
            return Result.failure(IllegalStateException("Insufficient available balance for withdrawal"))
        }

        val newBalance = user.walletBalance - amount
        val txn = WalletTransactionEntity(
            id = "TXN-WDL-${UUID.randomUUID().toString().take(8).uppercase()}",
            userId = user.id,
            type = "WITHDRAWAL_DEBIT",
            amount = amount,
            currency = user.preferredCurrency,
            status = "COMPLETED",
            referenceId = "WDL-${UUID.randomUUID().toString().take(6).uppercase()}",
            description = "Withdrawal to $destinationDetails",
            balanceAfter = newBalance,
            createdAt = System.currentTimeMillis()
        )

        db.withTransaction {
            userDao.updateBalances(user.id, newBalance, user.pendingBalance)
            walletDao.insertTransaction(txn)
        }

        notificationDao.insertNotification(
            NotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(6)}",
                userId = user.id,
                title = "Withdrawal Processed",
                message = "Withdrawal of ${user.preferredCurrency} $amount sent to $destinationDetails.",
                type = "WALLET_CREDIT"
            )
        )

        return Result.success(txn)
    }
}
