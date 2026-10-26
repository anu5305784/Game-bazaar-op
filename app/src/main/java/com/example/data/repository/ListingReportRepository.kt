package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.entities.ListingReportEntity
import com.example.data.local.entities.NotificationEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class ListingReportRepository(context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val reportDao = db.listingReportDao()
    private val notificationDao = db.notificationDao()

    fun observeAllReports(): Flow<List<ListingReportEntity>> = reportDao.observeAllReports()

    fun observePendingReports(): Flow<List<ListingReportEntity>> = reportDao.observePendingReports()

    suspend fun submitReport(
        listingId: String,
        listingTitle: String,
        reporterId: String,
        reporterEmail: String,
        reason: String,
        details: String
    ): Result<ListingReportEntity> {
        if (reason.isBlank() || details.isBlank()) {
            return Result.failure(IllegalArgumentException("Report reason and details are required"))
        }

        val report = ListingReportEntity(
            id = "REP-${UUID.randomUUID().toString().take(6).uppercase()}",
            listingId = listingId,
            listingTitle = listingTitle,
            reporterId = reporterId,
            reporterEmail = reporterEmail,
            reason = reason,
            details = details.trim(),
            status = "PENDING"
        )

        reportDao.insertReport(report)

        // Acknowledge report to reporter
        notificationDao.insertNotification(
            NotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(6)}",
                userId = reporterId,
                title = "Listing Report Received",
                message = "Thank you for reporting '$listingTitle'. Our trust & security team has queued it for review.",
                type = "SYSTEM"
            )
        )

        return Result.success(report)
    }
}
