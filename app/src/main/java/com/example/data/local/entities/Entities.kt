package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val username: String,
    val email: String,
    val passwordHash: String,
    val salt: String,
    val role: String, // "BUYER", "SELLER", "ADMIN"
    val displayName: String,
    val avatarUrl: String = "",
    val isEmailVerified: Boolean = false,
    val verificationCode: String = "",
    val preferredCurrency: String = "USD",
    val walletBalance: Double = 0.0, // Available funds
    val pendingBalance: Double = 0.0, // Pending clearance funds
    val isSuspended: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)


@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey val id: String,
    val title: String,
    val category: String,
    val publisher: String,
    val isPopular: Boolean = false,
    val iconUrl: String = "",
    val bannerUrl: String = "",
    val activeListingsCount: Int = 0
)

@Entity(tableName = "listings")
data class ListingEntity(
    @PrimaryKey val id: String,
    val sellerId: String,
    val sellerName: String,
    val sellerRating: Float = 4.9f,
    val gameId: String,
    val gameTitle: String,
    val title: String,
    val description: String,
    val originalPrice: Double,
    val originalCurrency: String,
    val accountLevel: Int = 1,
    val rank: String = "Unranked",
    val skinsCount: Int = 0,
    val charactersCount: Int = 0,
    val itemsDescription: String = "",
    val region: String = "Global",
    val status: String = "APPROVED", // "PENDING", "APPROVED", "REJECTED", "SOLD"
    val isFeatured: Boolean = false,
    val isShowcase: Boolean = false,
    val imageUrlsJson: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "delivery_vault")
data class DeliveryVaultEntity(
    @PrimaryKey val listingId: String,
    val encryptedCredentials: String,
    val transferMethod: String = "Account Credentials & Email Access",
    val transferInstructions: String = "",
    val isReleased: Boolean = false,
    val releasedToUserId: String? = null,
    val releasedAt: Long? = null
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: String,
    val orderNumber: String = "", // e.g. "GB-2026-X8921"
    val listingId: String,
    val listingTitle: String,
    val gameTitle: String,
    val buyerId: String,
    val buyerEmail: String,
    val sellerId: String,
    val sellerName: String,
    val amount: Double,
    val currency: String,
    val paymentMethod: String = "WALLET", // "WALLET", "UPI", "CRYPTO"
    val paymentStatus: String = "PAID", // "PENDING", "UNDER_REVIEW", "PAID", "FAILED"
    val deliveryStatus: String = "DELIVERED", // "PENDING", "PROCESSING", "DELIVERED"
    // Order statuses: "Pending Payment", "Payment Under Review", "Paid", "Processing", "Delivered", "Refunded", "Disputed"
    val status: String = "Delivered",
    val transactionReference: String = "",
    val utrNumber: String = "",
    val screenshotUrl: String = "",
    val rejectionReason: String = "",
    val disputeReason: String = "",
    val disputeStatus: String = "", // "NONE", "OPEN", "RESOLVED"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "wallet_transactions")
data class WalletTransactionEntity(
    @PrimaryKey val id: String, // Unique reference e.g. "TXN-WAL-91823"
    val userId: String,
    val type: String, // "DEPOSIT_UPI", "DEPOSIT_CRYPTO", "PAYMENT_DEBIT", "SALE_CREDIT", "REFUND_CREDIT", "WITHDRAWAL_DEBIT", "DISPUTE_HOLD"
    val amount: Double,
    val currency: String,
    val status: String = "COMPLETED", // "COMPLETED", "UNDER_REVIEW", "REJECTED", "FAILED"
    val referenceId: String = "", // Order Number or UTR
    val description: String = "",
    val balanceAfter: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorites", primaryKeys = ["userId", "listingId"])
data class FavoriteEntity(
    val userId: String,
    val listingId: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "support_tickets")
data class SupportTicketEntity(
    @PrimaryKey val id: String, // e.g. "TKT-89123"
    val userId: String,
    val userEmail: String,
    val orderId: String = "",
    val listingTitle: String = "",
    val subject: String,
    val message: String,
    val category: String = "GENERAL", // "DISPUTE", "ACCOUNT_DELIVERY", "PAYMENT", "REFUND", "GENERAL"
    val status: String = "OPEN", // "OPEN", "IN_REVIEW", "RESOLVED", "CLOSED"
    val adminResponse: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "upi_submissions")
data class UpiSubmissionEntity(
    @PrimaryKey val id: String, // e.g. "UPI-SUB-8912"
    val orderId: String,
    val userId: String,
    val userEmail: String,
    val amountInr: Double,
    val utrNumber: String,
    val merchantUpiId: String,
    val screenshotUrl: String = "",
    val status: String = "UNDER_REVIEW", // "UNDER_REVIEW", "APPROVED", "REJECTED"
    val rejectionReason: String = "",
    val reviewedByAdminId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val reviewedAt: Long? = null
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val message: String,
    val type: String, // "ORDER_STATUS", "PAYMENT_APPROVED", "PAYMENT_REJECTED", "DISPUTE_UPDATE", "WALLET_CREDIT", "SYSTEM"
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "admin_audit_logs")
data class AdminAuditLogEntity(
    @PrimaryKey val id: String, // e.g. "LOG-89123"
    val adminId: String,
    val adminName: String,
    val action: String, // e.g. "APPROVE_LISTING", "REJECT_LISTING", "APPROVE_UPI", "REJECT_UPI", "RESOLVE_DISPUTE_REFUND", "SUSPEND_USER", etc.
    val targetType: String, // "LISTING", "PAYMENT", "ORDER", "USER", "GAME", "REPORT"
    val targetId: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "listing_reports")
data class ListingReportEntity(
    @PrimaryKey val id: String, // e.g. "REP-7819"
    val listingId: String,
    val listingTitle: String,
    val reporterId: String,
    val reporterEmail: String,
    val reason: String, // "STOLEN_ACCOUNT", "MISLEADING_DESCRIPTION", "SUSPECTED_SCAM", "PROHIBITED_ITEMS", "OTHER"
    val details: String,
    val status: String = "PENDING", // "PENDING", "INVESTIGATING", "RESOLVED_DISMISSED", "RESOLVED_LISTING_REMOVED"
    val adminNotes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val resolvedAt: Long? = null
)

@Entity(tableName = "system_configs")
data class SystemConfigEntity(
    @PrimaryKey val key: String, // "UPI_MERCHANT_VPA", "UPI_ENABLED", "CRYPTO_ENABLED", "WALLET_ENABLED"
    val value: String,
    val description: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

