package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.AdminAuditLogEntity
import com.example.data.local.entities.DeliveryVaultEntity
import com.example.data.local.entities.FavoriteEntity
import com.example.data.local.entities.GameEntity
import com.example.data.local.entities.ListingEntity
import com.example.data.local.entities.ListingReportEntity
import com.example.data.local.entities.NotificationEntity
import com.example.data.local.entities.OrderEntity
import com.example.data.local.entities.SupportTicketEntity
import com.example.data.local.entities.SystemConfigEntity
import com.example.data.local.entities.UpiSubmissionEntity
import com.example.data.local.entities.UserEntity
import com.example.data.local.entities.WalletTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id")
    fun observeUser(id: String): Flow<UserEntity?>

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun observeAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    suspend fun getAllUsers(): List<UserEntity>

    @Query("UPDATE users SET walletBalance = :available, pendingBalance = :pending WHERE id = :userId")
    suspend fun updateBalances(userId: String, available: Double, pending: Double)

    @Query("UPDATE users SET isSuspended = :isSuspended WHERE id = :userId")
    suspend fun updateSuspended(userId: String, isSuspended: Boolean)

    @Query("UPDATE users SET role = :role WHERE id = :userId")
    suspend fun updateRole(userId: String, role: String)

    @Query("SELECT COUNT(*) FROM users WHERE role = 'ADMIN'")
    suspend fun getAdminCount(): Int
}

@Dao
interface GameDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(games: List<GameEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGame(game: GameEntity)

    @Update
    suspend fun updateGame(game: GameEntity)

    @Delete
    suspend fun deleteGame(game: GameEntity)

    @Query("SELECT COUNT(*) FROM games")
    suspend fun getGameCount(): Int

    @Query("SELECT * FROM games ORDER BY isPopular DESC, title ASC")
    fun observeAllGames(): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE isPopular = 1 ORDER BY activeListingsCount DESC, title ASC LIMIT 20")
    fun observePopularGames(): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE category = :category ORDER BY title ASC")
    fun observeGamesByCategory(category: String): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE title LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' ORDER BY isPopular DESC, title ASC")
    fun searchGames(query: String): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE id = :id LIMIT 1")
    suspend fun getGameById(id: String): GameEntity?
}

@Dao
interface ListingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(listings: List<ListingEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertListing(listing: ListingEntity)

    @Update
    suspend fun updateListing(listing: ListingEntity)

    @Delete
    suspend fun deleteListing(listing: ListingEntity)

    @Query("SELECT * FROM listings WHERE id = :id LIMIT 1")
    suspend fun getListingById(id: String): ListingEntity?

    @Query("SELECT * FROM listings WHERE id = :id LIMIT 1")
    fun observeListingById(id: String): Flow<ListingEntity?>

    @Query("SELECT * FROM listings WHERE status = 'APPROVED' ORDER BY createdAt DESC")
    fun observeRecentApprovedListings(): Flow<List<ListingEntity>>

    @Query("SELECT * FROM listings WHERE status = 'APPROVED' AND isFeatured = 1 ORDER BY createdAt DESC")
    fun observeFeaturedListings(): Flow<List<ListingEntity>>

    @Query("SELECT * FROM listings WHERE sellerId = :sellerId ORDER BY createdAt DESC")
    fun observeSellerListings(sellerId: String): Flow<List<ListingEntity>>

    @Query("SELECT * FROM listings ORDER BY createdAt DESC")
    fun observeAllListingsForAdmin(): Flow<List<ListingEntity>>

    @Query("SELECT * FROM listings WHERE status = 'PENDING' ORDER BY createdAt DESC")
    fun observePendingListingsForAdmin(): Flow<List<ListingEntity>>

    @Query("SELECT COUNT(*) FROM listings")
    suspend fun getListingCount(): Int

    @Query("UPDATE listings SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE listings SET isShowcase = :isShowcase, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateShowcase(id: String, isShowcase: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE listings SET isFeatured = :isFeatured, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateFeatured(id: String, isFeatured: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT * FROM listings WHERE id IN (SELECT listingId FROM favorites WHERE userId = :userId)")
    fun observeFavoriteListings(userId: String): Flow<List<ListingEntity>>
}

@Dao
interface DeliveryVaultDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVault(vault: DeliveryVaultEntity)

    @Query("SELECT * FROM delivery_vault WHERE listingId = :listingId LIMIT 1")
    suspend fun getVaultByListingId(listingId: String): DeliveryVaultEntity?

    @Query("UPDATE delivery_vault SET isReleased = 1, releasedToUserId = :buyerId, releasedAt = :timestamp WHERE listingId = :listingId")
    suspend fun markReleased(listingId: String, buyerId: String, timestamp: Long = System.currentTimeMillis())
}

@Dao
interface OrderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Query("SELECT * FROM orders WHERE buyerId = :buyerId ORDER BY createdAt DESC")
    fun observeOrdersForBuyer(buyerId: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE sellerId = :sellerId ORDER BY createdAt DESC")
    fun observeOrdersForSeller(sellerId: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun observeAllOrdersForAdmin(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    suspend fun getAllOrders(): List<OrderEntity>

    @Query("SELECT * FROM orders WHERE id = :id LIMIT 1")
    suspend fun getOrderById(id: String): OrderEntity?

    @Query("SELECT * FROM orders WHERE id = :id LIMIT 1")
    fun observeOrderById(id: String): Flow<OrderEntity?>

    @Query("SELECT * FROM orders WHERE orderNumber = :orderNumber LIMIT 1")
    suspend fun getOrderByNumber(orderNumber: String): OrderEntity?

    @Query("UPDATE orders SET status = :status, paymentStatus = :paymentStatus, deliveryStatus = :deliveryStatus, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStatusFields(
        id: String,
        status: String,
        paymentStatus: String,
        deliveryStatus: String,
        updatedAt: Long = System.currentTimeMillis()
    )
}

@Dao
interface WalletTransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(txn: WalletTransactionEntity)

    @Query("SELECT * FROM wallet_transactions WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeTransactionsForUser(userId: String): Flow<List<WalletTransactionEntity>>

    @Query("SELECT * FROM wallet_transactions ORDER BY createdAt DESC")
    fun observeAllTransactionsForAdmin(): Flow<List<WalletTransactionEntity>>

    @Query("SELECT * FROM wallet_transactions ORDER BY createdAt DESC")
    suspend fun getAllTransactions(): List<WalletTransactionEntity>

    @Query("SELECT * FROM wallet_transactions WHERE id = :id LIMIT 1")
    suspend fun getTransactionById(id: String): WalletTransactionEntity?

    @Query("SELECT * FROM wallet_transactions WHERE referenceId = :refId LIMIT 1")
    suspend fun getTransactionByReference(refId: String): WalletTransactionEntity?
}

@Dao
interface FavoriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(fav: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE userId = :userId AND listingId = :listingId")
    suspend fun removeFavorite(userId: String, listingId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE userId = :userId AND listingId = :listingId)")
    fun isFavorite(userId: String, listingId: String): Flow<Boolean>

    @Query("SELECT * FROM favorites WHERE userId = :userId")
    fun observeFavorites(userId: String): Flow<List<FavoriteEntity>>
}

@Dao
interface SupportTicketDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTicket(ticket: SupportTicketEntity)

    @Update
    suspend fun updateTicket(ticket: SupportTicketEntity)

    @Query("SELECT * FROM support_tickets WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeTicketsForUser(userId: String): Flow<List<SupportTicketEntity>>

    @Query("SELECT * FROM support_tickets ORDER BY createdAt DESC")
    fun observeAllTicketsForAdmin(): Flow<List<SupportTicketEntity>>

    @Query("SELECT * FROM support_tickets WHERE id = :id LIMIT 1")
    suspend fun getTicketById(id: String): SupportTicketEntity?
}

@Dao
interface UpiSubmissionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubmission(sub: UpiSubmissionEntity)

    @Update
    suspend fun updateSubmission(sub: UpiSubmissionEntity)

    @Query("SELECT * FROM upi_submissions ORDER BY createdAt DESC")
    fun observeAllSubmissions(): Flow<List<UpiSubmissionEntity>>

    @Query("SELECT * FROM upi_submissions WHERE status = 'UNDER_REVIEW' ORDER BY createdAt DESC")
    fun observePendingSubmissions(): Flow<List<UpiSubmissionEntity>>

    @Query("SELECT * FROM upi_submissions WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeSubmissionsForUser(userId: String): Flow<List<UpiSubmissionEntity>>

    @Query("SELECT * FROM upi_submissions WHERE orderId = :orderId LIMIT 1")
    suspend fun getSubmissionByOrderId(orderId: String): UpiSubmissionEntity?

    @Query("SELECT * FROM upi_submissions WHERE id = :id LIMIT 1")
    suspend fun getSubmissionById(id: String): UpiSubmissionEntity?
}

@Dao
interface NotificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeNotifications(userId: String): Flow<List<NotificationEntity>>

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllAsRead(userId: String)
}

@Dao
interface AdminAuditLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AdminAuditLogEntity)

    @Query("SELECT * FROM admin_audit_logs ORDER BY timestamp DESC")
    fun observeAllLogs(): Flow<List<AdminAuditLogEntity>>

    @Query("SELECT * FROM admin_audit_logs ORDER BY timestamp DESC")
    suspend fun getAllLogs(): List<AdminAuditLogEntity>
}

@Dao
interface ListingReportDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ListingReportEntity)

    @Update
    suspend fun updateReport(report: ListingReportEntity)

    @Query("SELECT * FROM listing_reports ORDER BY createdAt DESC")
    fun observeAllReports(): Flow<List<ListingReportEntity>>

    @Query("SELECT * FROM listing_reports WHERE status = 'PENDING' ORDER BY createdAt DESC")
    fun observePendingReports(): Flow<List<ListingReportEntity>>

    @Query("SELECT * FROM listing_reports WHERE id = :id LIMIT 1")
    suspend fun getReportById(id: String): ListingReportEntity?
}

@Dao
interface SystemConfigDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(config: SystemConfigEntity)

    @Query("SELECT * FROM system_configs WHERE key = :key LIMIT 1")
    suspend fun getConfig(key: String): SystemConfigEntity?

    @Query("SELECT * FROM system_configs")
    fun observeAllConfigs(): Flow<List<SystemConfigEntity>>
}

