package com.example.data.repository

import android.content.Context
import com.example.data.currency.CurrencyManager
import com.example.data.local.AppDatabase
import com.example.data.local.entities.DeliveryVaultEntity
import com.example.data.local.entities.ListingEntity
import com.example.data.local.entities.NotificationEntity
import com.example.data.security.SecurityUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

data class ListingFilter(
    val query: String = "",
    val gameTitle: String = "All Games",
    val category: String = "All",
    val region: String = "All Regions",
    val currency: String = "All",
    val minPriceUsd: Double? = null,
    val maxPriceUsd: Double? = null,
    val sortBy: SortOption = SortOption.NEWEST
)

enum class SortOption(val label: String) {
    NEWEST("Recently Added"),
    PRICE_LOW_HIGH("Price: Low to High"),
    PRICE_HIGH_LOW("Price: High to Low"),
    ACCOUNT_LEVEL("Highest Level")
}

class ListingRepository(context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val listingDao = db.listingDao()
    private val deliveryVaultDao = db.deliveryVaultDao()
    private val notificationDao = db.notificationDao()

    fun getApprovedListings(): Flow<List<ListingEntity>> = listingDao.observeRecentApprovedListings()

    fun getFeaturedListings(): Flow<List<ListingEntity>> = listingDao.observeFeaturedListings()

    fun getSellerListings(sellerId: String): Flow<List<ListingEntity>> = listingDao.observeSellerListings(sellerId)

    fun getAdminListings(): Flow<List<ListingEntity>> = listingDao.observeAllListingsForAdmin()

    fun getPendingListings(): Flow<List<ListingEntity>> = listingDao.observePendingListingsForAdmin()

    fun observeListingById(id: String): Flow<ListingEntity?> = listingDao.observeListingById(id)

    suspend fun getListingById(id: String): ListingEntity? = listingDao.getListingById(id)

    fun getFilteredListings(filter: ListingFilter): Flow<List<ListingEntity>> {
        return listingDao.observeRecentApprovedListings().map { list ->
            list.filter { listing ->
                // Search query match
                val matchesQuery = filter.query.isBlank() ||
                    listing.title.contains(filter.query, ignoreCase = true) ||
                    listing.gameTitle.contains(filter.query, ignoreCase = true) ||
                    listing.rank.contains(filter.query, ignoreCase = true) ||
                    listing.description.contains(filter.query, ignoreCase = true)

                // Game filter
                val matchesGame = filter.gameTitle == "All Games" ||
                    listing.gameTitle.equals(filter.gameTitle, ignoreCase = true)

                // Region filter
                val matchesRegion = filter.region == "All Regions" ||
                    listing.region.equals(filter.region, ignoreCase = true)

                // Currency filter
                val matchesCurrency = filter.currency == "All" ||
                    listing.originalCurrency.equals(filter.currency, ignoreCase = true)

                // Price filter in USD normalization
                val priceInUsd = CurrencyManager.convert(listing.originalPrice, listing.originalCurrency, "USD")
                val matchesMin = filter.minPriceUsd == null || priceInUsd >= filter.minPriceUsd
                val matchesMax = filter.maxPriceUsd == null || priceInUsd <= filter.maxPriceUsd

                matchesQuery && matchesGame && matchesRegion && matchesCurrency && matchesMin && matchesMax
            }.sortedWith { a, b ->
                when (filter.sortBy) {
                    SortOption.NEWEST -> b.createdAt.compareTo(a.createdAt)
                    SortOption.PRICE_LOW_HIGH -> {
                        val usdPriceA = CurrencyManager.convert(a.originalPrice, a.originalCurrency, "USD")
                        val usdPriceB = CurrencyManager.convert(b.originalPrice, b.originalCurrency, "USD")
                        usdPriceA.compareTo(usdPriceB)
                    }
                    SortOption.PRICE_HIGH_LOW -> {
                        val usdPriceA = CurrencyManager.convert(a.originalPrice, a.originalCurrency, "USD")
                        val usdPriceB = CurrencyManager.convert(b.originalPrice, b.originalCurrency, "USD")
                        usdPriceB.compareTo(usdPriceA)
                    }
                    SortOption.ACCOUNT_LEVEL -> b.accountLevel.compareTo(a.accountLevel)
                }
            }
        }
    }

    suspend fun createListing(
        sellerId: String,
        sellerName: String,
        gameId: String,
        gameTitle: String,
        title: String,
        description: String,
        originalPrice: Double,
        originalCurrency: String,
        accountLevel: Int,
        rank: String,
        skinsCount: Int,
        charactersCount: Int,
        itemsDescription: String,
        region: String,
        imageUrls: List<String>,
        deliveryCredentialsPlain: String,
        transferInstructions: String,
        autoApprove: Boolean = false
    ): Result<ListingEntity> {
        if (title.isBlank() || description.isBlank() || originalPrice <= 0.0) {
            return Result.failure(IllegalArgumentException("Please complete title, description, and a valid price"))
        }
        if (deliveryCredentialsPlain.isBlank()) {
            return Result.failure(IllegalArgumentException("Protected delivery credentials are required for account transfer"))
        }

        val listingId = "lst_${UUID.randomUUID().toString().take(8)}"
        val status = if (autoApprove) "APPROVED" else "PENDING"

        val listing = ListingEntity(
            id = listingId,
            sellerId = sellerId,
            sellerName = sellerName,
            gameId = gameId,
            gameTitle = gameTitle,
            title = title.trim(),
            description = description.trim(),
            originalPrice = originalPrice,
            originalCurrency = originalCurrency.uppercase(),
            accountLevel = accountLevel,
            rank = rank.ifBlank { "Unranked" },
            skinsCount = skinsCount,
            charactersCount = charactersCount,
            itemsDescription = itemsDescription.trim(),
            region = region,
            status = status,
            isFeatured = false,
            isShowcase = false,
            imageUrlsJson = imageUrls.joinToString(","),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        // Store encrypted delivery vault
        val vault = DeliveryVaultEntity(
            listingId = listingId,
            encryptedCredentials = SecurityUtils.encryptDeliveryData(deliveryCredentialsPlain),
            transferMethod = "Verified Account Credentials",
            transferInstructions = transferInstructions.ifBlank { "Credentials will be automatically revealed to the buyer upon verified checkout." },
            isReleased = false
        )

        listingDao.insertListing(listing)
        deliveryVaultDao.insertVault(vault)

        // Notification to seller
        notificationDao.insertNotification(
            NotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(6)}",
                userId = sellerId,
                title = if (status == "APPROVED") "Listing Published" else "Listing Submitted for Review",
                message = "Your listing for '$title' ($gameTitle) is now $status.",
                type = "LISTING_APPROVED"
            )
        )

        return Result.success(listing)
    }

    suspend fun updateListing(listing: ListingEntity): Result<Unit> {
        listingDao.updateListing(listing.copy(updatedAt = System.currentTimeMillis()))
        return Result.success(Unit)
    }

    suspend fun deleteListing(listing: ListingEntity): Result<Unit> {
        listingDao.deleteListing(listing)
        return Result.success(Unit)
    }

    suspend fun approveListing(listingId: String, adminId: String = "usr_admin_01", adminName: String = "Admin"): Result<Unit> {
        val listing = listingDao.getListingById(listingId) ?: return Result.failure(IllegalArgumentException("Listing not found"))
        listingDao.updateStatus(listingId, "APPROVED")
        notificationDao.insertNotification(
            NotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(6)}",
                userId = listing.sellerId,
                title = "Listing Approved!",
                message = "Your listing for '${listing.title}' has been approved and is now live on Gamer Bazaar.",
                type = "LISTING_APPROVED"
            )
        )
        db.adminAuditDao().insertAuditLog(
            com.example.data.local.entities.AdminAuditLogEntity(
                id = "LOG-LST-${System.currentTimeMillis()}",
                adminId = adminId,
                adminName = adminName,
                action = "APPROVE_LISTING",
                targetType = "LISTING",
                targetId = listingId,
                details = "Listing '${listing.title}' approved."
            )
        )
        return Result.success(Unit)
    }

    suspend fun rejectListing(
        listingId: String,
        reason: String = "Policy violation or incomplete credentials",
        adminId: String = "usr_admin_01",
        adminName: String = "Admin"
    ): Result<Unit> {
        val listing = listingDao.getListingById(listingId) ?: return Result.failure(IllegalArgumentException("Listing not found"))
        listingDao.updateStatus(listingId, "REJECTED")
        notificationDao.insertNotification(
            NotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(6)}",
                userId = listing.sellerId,
                title = "Listing Rejected",
                message = "Your listing '${listing.title}' was rejected: $reason",
                type = "SYSTEM"
            )
        )
        db.adminAuditDao().insertAuditLog(
            com.example.data.local.entities.AdminAuditLogEntity(
                id = "LOG-LST-${System.currentTimeMillis()}",
                adminId = adminId,
                adminName = adminName,
                action = "REJECT_LISTING",
                targetType = "LISTING",
                targetId = listingId,
                details = "Listing '${listing.title}' rejected. Reason: $reason"
            )
        )
        return Result.success(Unit)
    }

    suspend fun toggleShowcase(
        listingId: String,
        isShowcase: Boolean,
        adminId: String = "usr_admin_01",
        adminName: String = "Admin"
    ): Result<Unit> {
        listingDao.updateShowcase(listingId, isShowcase)
        db.adminAuditDao().insertAuditLog(
            com.example.data.local.entities.AdminAuditLogEntity(
                id = "LOG-SHOW-${System.currentTimeMillis()}",
                adminId = adminId,
                adminName = adminName,
                action = "TOGGLE_SHOWCASE",
                targetType = "LISTING",
                targetId = listingId,
                details = "Listing $listingId showcase set to $isShowcase"
            )
        )
        return Result.success(Unit)
    }

    suspend fun toggleFeatured(
        listingId: String,
        isFeatured: Boolean,
        adminId: String = "usr_admin_01",
        adminName: String = "Admin"
    ): Result<Unit> {
        listingDao.updateFeatured(listingId, isFeatured)
        db.adminAuditDao().insertAuditLog(
            com.example.data.local.entities.AdminAuditLogEntity(
                id = "LOG-FEAT-${System.currentTimeMillis()}",
                adminId = adminId,
                adminName = adminName,
                action = "TOGGLE_FEATURED",
                targetType = "LISTING",
                targetId = listingId,
                details = "Listing $listingId featured set to $isFeatured"
            )
        )
        return Result.success(Unit)
    }
}

