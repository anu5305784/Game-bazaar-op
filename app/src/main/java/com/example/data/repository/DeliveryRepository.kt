package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.security.SecurityUtils

data class DecryptedDeliveryInfo(
    val listingId: String,
    val transferMethod: String,
    val credentials: String,
    val instructions: String,
    val isReleased: Boolean,
    val releasedAt: Long?
)

class DeliveryRepository(context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val deliveryVaultDao = db.deliveryVaultDao()
    private val listingDao = db.listingDao()

    /**
     * Strictly controls access to sensitive account delivery credentials.
     * Decrypts credentials ONLY when caller is:
     * 1. The original Seller who created the listing
     * 2. An Admin
     * 3. The verified Buyer after the order is completed and released
     */
    suspend fun getAuthorizedDeliveryInfo(
        listingId: String,
        requestingUserId: String,
        requestingUserRole: String
    ): Result<DecryptedDeliveryInfo> {
        val listing = listingDao.getListingById(listingId)
            ?: return Result.failure(IllegalArgumentException("Listing not found"))

        val vault = deliveryVaultDao.getVaultByListingId(listingId)
            ?: return Result.failure(IllegalStateException("No delivery credentials registered for this listing"))

        val isAdmin = requestingUserRole.equals("ADMIN", ignoreCase = true)
        val isSeller = listing.sellerId == requestingUserId
        val isAuthorizedBuyer = vault.isReleased && vault.releasedToUserId == requestingUserId

        if (!isAdmin && !isSeller && !isAuthorizedBuyer) {
            return Result.failure(
                SecurityException("Access Denied: Protected credentials are only accessible to the seller, administrator, or verified buyer post-payment.")
            )
        }

        val decrypted = SecurityUtils.decryptDeliveryData(vault.encryptedCredentials)
        return Result.success(
            DecryptedDeliveryInfo(
                listingId = vault.listingId,
                transferMethod = vault.transferMethod,
                credentials = decrypted,
                instructions = vault.transferInstructions,
                isReleased = vault.isReleased,
                releasedAt = vault.releasedAt
            )
        )
    }
}
