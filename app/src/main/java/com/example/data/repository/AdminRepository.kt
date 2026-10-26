package com.example.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.example.data.local.AppDatabase
import com.example.data.local.entities.AdminAuditLogEntity
import com.example.data.local.entities.GameEntity
import com.example.data.local.entities.ListingReportEntity
import com.example.data.local.entities.NotificationEntity
import com.example.data.local.entities.SystemConfigEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class AdminRepository(context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val auditDao = db.adminAuditDao()
    private val reportDao = db.listingReportDao()
    private val configDao = db.systemConfigDao()
    private val gameDao = db.gameDao()
    private val listingDao = db.listingDao()
    private val orderDao = db.orderDao()
    private val walletDao = db.walletDao()
    private val notificationDao = db.notificationDao()

    fun observeAuditLogs(): Flow<List<AdminAuditLogEntity>> = auditDao.observeAllLogs()

    fun observeSystemConfigs(): Flow<List<SystemConfigEntity>> = configDao.observeAllConfigs()

    fun observeAllReports(): Flow<List<ListingReportEntity>> = reportDao.observeAllReports()

    suspend fun logAction(
        adminId: String,
        adminName: String,
        action: String,
        targetType: String,
        targetId: String,
        details: String
    ) {
        auditDao.insertAuditLog(
            AdminAuditLogEntity(
                id = "LOG-${UUID.randomUUID().toString().take(8).uppercase()}",
                adminId = adminId,
                adminName = adminName,
                action = action,
                targetType = targetType,
                targetId = targetId,
                details = details
            )
        )
    }

    suspend fun updateConfig(key: String, value: String, description: String, adminId: String, adminName: String) {
        configDao.insertConfig(
            SystemConfigEntity(
                key = key,
                value = value,
                description = description,
                updatedAt = System.currentTimeMillis()
            )
        )
        logAction(
            adminId = adminId,
            adminName = adminName,
            action = "UPDATE_SYSTEM_CONFIG",
            targetType = "CONFIG",
            targetId = key,
            details = "Config key '$key' set to '$value'"
        )
    }

    suspend fun addOrUpdateGame(
        game: GameEntity,
        adminId: String,
        adminName: String
    ): Result<Unit> {
        if (game.title.isBlank() || game.category.isBlank()) {
            return Result.failure(IllegalArgumentException("Game title and category are required"))
        }
        gameDao.insertGame(game)
        logAction(
            adminId = adminId,
            adminName = adminName,
            action = "MANAGE_GAME",
            targetType = "GAME",
            targetId = game.id,
            details = "Game catalog updated: ${game.title} (${game.category})"
        )
        return Result.success(Unit)
    }

    suspend fun resolveListingReport(
        reportId: String,
        action: String, // "DISMISSED" or "LISTING_REMOVED"
        notes: String,
        adminId: String,
        adminName: String
    ): Result<Unit> {
        val report = reportDao.getReportById(reportId)
            ?: return Result.failure(IllegalArgumentException("Report not found"))

        db.withTransaction {
            val status = if (action == "LISTING_REMOVED") "RESOLVED_LISTING_REMOVED" else "RESOLVED_DISMISSED"
            reportDao.updateReport(
                report.copy(
                    status = status,
                    adminNotes = notes,
                    resolvedAt = System.currentTimeMillis()
                )
            )

            if (action == "LISTING_REMOVED") {
                listingDao.updateStatus(report.listingId, "REJECTED")
            }

            notificationDao.insertNotification(
                NotificationEntity(
                    id = "notif_${UUID.randomUUID().toString().take(6)}",
                    userId = report.reporterId,
                    title = "Report Review Completed",
                    message = "Your report on '${report.listingTitle}' has been investigated: $notes.",
                    type = "SYSTEM"
                )
            )

            logAction(
                adminId = adminId,
                adminName = adminName,
                action = "RESOLVE_REPORT",
                targetType = "LISTING_REPORT",
                targetId = reportId,
                details = "Report on '${report.listingTitle}' resolved as $status. Notes: $notes"
            )
        }

        return Result.success(Unit)
    }

    suspend fun exportOrdersCsv(): String {
        val orders = orderDao.getAllOrders()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        val sb = StringBuilder()
        sb.append("OrderNumber,ListingTitle,GameTitle,Amount,Currency,PaymentMethod,Status,PaymentStatus,BuyerEmail,CreatedAt\n")
        for (o in orders) {
            val formattedDate = dateFormat.format(Date(o.createdAt))
            val cleanTitle = o.listingTitle.replace(",", " ")
            val cleanGame = o.gameTitle.replace(",", " ")
            sb.append("${o.orderNumber},\"$cleanTitle\",\"$cleanGame\",${o.amount},${o.currency},${o.paymentMethod},${o.status},${o.paymentStatus},${o.buyerEmail},$formattedDate\n")
        }
        return sb.toString()
    }

    suspend fun exportTransactionsCsv(): String {
        val txns = walletDao.getAllTransactions()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        val sb = StringBuilder()
        sb.append("TxnId,UserId,Type,Amount,Currency,Status,ReferenceId,BalanceAfter,CreatedAt\n")
        for (t in txns) {
            val formattedDate = dateFormat.format(Date(t.createdAt))
            val cleanRef = t.referenceId.replace(",", " ")
            sb.append("${t.id},${t.userId},${t.type},${t.amount},${t.currency},${t.status},\"$cleanRef\",${t.balanceAfter},$formattedDate\n")
        }
        return sb.toString()
    }

    suspend fun exportAuditLogsCsv(): String {
        val logs = auditDao.getAllLogs()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        val sb = StringBuilder()
        sb.append("LogId,AdminId,AdminName,Action,TargetType,TargetId,Details,Timestamp\n")
        for (l in logs) {
            val formattedDate = dateFormat.format(Date(l.timestamp))
            val cleanDetails = l.details.replace(",", ";").replace("\"", "'")
            sb.append("${l.id},${l.adminId},\"${l.adminName}\",${l.action},${l.targetType},${l.targetId},\"$cleanDetails\",$formattedDate\n")
        }
        return sb.toString()
    }
}
