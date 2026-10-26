package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.AdminAuditLogDao
import com.example.data.local.dao.DeliveryVaultDao
import com.example.data.local.dao.FavoriteDao
import com.example.data.local.dao.GameDao
import com.example.data.local.dao.ListingDao
import com.example.data.local.dao.ListingReportDao
import com.example.data.local.dao.NotificationDao
import com.example.data.local.dao.OrderDao
import com.example.data.local.dao.SupportTicketDao
import com.example.data.local.dao.SystemConfigDao
import com.example.data.local.dao.UpiSubmissionDao
import com.example.data.local.dao.UserDao
import com.example.data.local.dao.WalletTransactionDao
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
import com.example.data.local.seed.DefaultGamesData
import com.example.data.local.seed.DefaultListingsData
import com.example.data.security.SecurityUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        GameEntity::class,
        ListingEntity::class,
        DeliveryVaultEntity::class,
        OrderEntity::class,
        WalletTransactionEntity::class,
        FavoriteEntity::class,
        SupportTicketEntity::class,
        UpiSubmissionEntity::class,
        NotificationEntity::class,
        AdminAuditLogEntity::class,
        ListingReportEntity::class,
        SystemConfigEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun gameDao(): GameDao
    abstract fun listingDao(): ListingDao
    abstract fun deliveryVaultDao(): DeliveryVaultDao
    abstract fun orderDao(): OrderDao
    abstract fun walletDao(): WalletTransactionDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun supportTicketDao(): SupportTicketDao
    abstract fun upiSubmissionDao(): UpiSubmissionDao
    abstract fun notificationDao(): NotificationDao
    abstract fun adminAuditDao(): AdminAuditLogDao
    abstract fun listingReportDao(): ListingReportDao
    abstract fun systemConfigDao(): SystemConfigDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gamer_bazaar.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                INSTANCE?.let { database ->
                    seedInitialData(database)
                }
            }
        }

        private suspend fun seedInitialData(database: AppDatabase) {
            // Seed 360+ Game Catalog
            database.gameDao().insertAll(DefaultGamesData.ALL_GAMES)

            // Seed Admin, Seller, and Buyer default accounts
            val adminSalt = SecurityUtils.generateSalt()
            val sellerSalt = SecurityUtils.generateSalt()
            val buyerSalt = SecurityUtils.generateSalt()

            val adminUser = UserEntity(
                id = "usr_admin_01",
                username = "admin",
                email = "admin@gamerbazaar.com",
                passwordHash = SecurityUtils.hashPassword("Admin@1234", adminSalt),
                salt = adminSalt,
                role = "ADMIN",
                displayName = "Gamer Bazaar Admin",
                isEmailVerified = true,
                preferredCurrency = "USD",
                walletBalance = 15000.0,
                pendingBalance = 0.0
            )

            val sellerUser = UserEntity(
                id = "usr_seller_01",
                username = "apex_trader",
                email = "seller@gamerbazaar.com",
                passwordHash = SecurityUtils.hashPassword("Seller@1234", sellerSalt),
                salt = sellerSalt,
                role = "SELLER",
                displayName = "ApexTrader Official",
                isEmailVerified = true,
                preferredCurrency = "INR",
                walletBalance = 42500.0,
                pendingBalance = 0.0
            )

            val buyerUser = UserEntity(
                id = "usr_buyer_01",
                username = "shadow_buyer",
                email = "buyer@gamerbazaar.com",
                passwordHash = SecurityUtils.hashPassword("Buyer@1234", buyerSalt),
                salt = buyerSalt,
                role = "BUYER",
                displayName = "Shadow Gamer",
                isEmailVerified = true,
                preferredCurrency = "USD",
                walletBalance = 1250.0,
                pendingBalance = 0.0
            )

            database.userDao().insertUser(adminUser)
            database.userDao().insertUser(sellerUser)
            database.userDao().insertUser(buyerUser)

            // Seed listings & delivery vaults
            database.listingDao().insertAll(DefaultListingsData.INITIAL_LISTINGS)
            DefaultListingsData.createInitialVaults().forEach { vault ->
                database.deliveryVaultDao().insertVault(vault)
            }

            // Seed initial wallet ledger record for buyer
            database.walletDao().insertTransaction(
                WalletTransactionEntity(
                    id = "TXN-WAL-WELCOME",
                    userId = "usr_buyer_01",
                    type = "DEPOSIT_UPI",
                    amount = 1250.0,
                    currency = "USD",
                    status = "COMPLETED",
                    referenceId = "WELCOME_BONUS",
                    description = "Initial escrow deposit balance",
                    balanceAfter = 1250.0,
                    createdAt = System.currentTimeMillis() - 86400000
                )
            )

            // Seed initial favorite
            database.favoriteDao().addFavorite(
                FavoriteEntity(
                    userId = "usr_buyer_01",
                    listingId = "lst_coc_th16"
                )
            )

            // Seed welcome notification
            database.notificationDao().insertNotification(
                NotificationEntity(
                    id = "notif_welcome",
                    userId = "usr_buyer_01",
                    title = "Welcome to Gamer Bazaar!",
                    message = "Browse 350+ game accounts worldwide. Instant escrow protection on all purchases.",
                    type = "SYSTEM"
                )
            )

            // Seed default payment and system configs
            database.systemConfigDao().insertConfig(
                SystemConfigEntity(
                    key = "UPI_MERCHANT_VPA",
                    value = "gamerbazaar.escrow@icici",
                    description = "Configured Merchant UPI VPA for Indian Payments"
                )
            )
            database.systemConfigDao().insertConfig(
                SystemConfigEntity(
                    key = "UPI_ENABLED",
                    value = "true",
                    description = "Enable UPI Payment Gateway"
                )
            )
            database.systemConfigDao().insertConfig(
                SystemConfigEntity(
                    key = "CRYPTO_ENABLED",
                    value = "true",
                    description = "Enable Cryptocurrency Payments (USDT, BTC, ETH, SOL)"
                )
            )
            database.systemConfigDao().insertConfig(
                SystemConfigEntity(
                    key = "WALLET_ENABLED",
                    value = "true",
                    description = "Enable Instant Wallet Escrow Checkout"
                )
            )

            // Seed initial administrative audit log
            database.adminAuditDao().insertAuditLog(
                AdminAuditLogEntity(
                    id = "LOG-INIT-001",
                    adminId = "usr_admin_01",
                    adminName = "Gamer Bazaar Admin",
                    action = "BOOTSTRAP_SYSTEM",
                    targetType = "SYSTEM",
                    targetId = "PLATFORM_ROOT",
                    details = "Platform initialized with 360+ verified game catalog, multi-currency escrow, and immutable ledger."
                )
            )
        }
    }
}

