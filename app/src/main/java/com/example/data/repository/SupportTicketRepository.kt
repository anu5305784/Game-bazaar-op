package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.entities.NotificationEntity
import com.example.data.local.entities.SupportTicketEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class SupportTicketRepository(context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val ticketDao = db.supportTicketDao()
    private val notificationDao = db.notificationDao()

    fun observeTicketsForUser(userId: String): Flow<List<SupportTicketEntity>> {
        return ticketDao.observeTicketsForUser(userId)
    }

    fun observeAllTicketsForAdmin(): Flow<List<SupportTicketEntity>> {
        return ticketDao.observeAllTicketsForAdmin()
    }

    suspend fun createTicket(
        userId: String,
        userEmail: String,
        orderId: String = "",
        listingTitle: String = "",
        subject: String,
        message: String,
        category: String = "GENERAL"
    ): Result<SupportTicketEntity> {
        if (subject.isBlank() || message.isBlank()) {
            return Result.failure(IllegalArgumentException("Subject and message are required"))
        }

        val ticketId = "TKT-${UUID.randomUUID().toString().take(6).uppercase()}"
        val ticket = SupportTicketEntity(
            id = ticketId,
            userId = userId,
            userEmail = userEmail,
            orderId = orderId,
            listingTitle = listingTitle,
            subject = subject.trim(),
            message = message.trim(),
            category = category,
            status = "OPEN",
            adminResponse = "",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        ticketDao.insertTicket(ticket)

        notificationDao.insertNotification(
            NotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(6)}",
                userId = userId,
                title = "Support Ticket Created ($ticketId)",
                message = "Your inquiry '$subject' has been submitted to the support resolution desk.",
                type = "SYSTEM"
            )
        )

        return Result.success(ticket)
    }

    suspend fun replyTicket(
        ticketId: String,
        adminResponse: String,
        newStatus: String = "RESOLVED"
    ): Result<Unit> {
        val ticket = ticketDao.getTicketById(ticketId)
            ?: return Result.failure(IllegalArgumentException("Ticket not found"))

        val updated = ticket.copy(
            adminResponse = adminResponse.trim(),
            status = newStatus,
            updatedAt = System.currentTimeMillis()
        )
        ticketDao.updateTicket(updated)

        notificationDao.insertNotification(
            NotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(6)}",
                userId = ticket.userId,
                title = "Support Ticket Update ($ticketId)",
                message = "Support response received: $adminResponse",
                type = "DISPUTE_UPDATE"
            )
        )

        return Result.success(Unit)
    }
}
