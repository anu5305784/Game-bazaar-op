package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.entities.FavoriteEntity
import com.example.data.local.entities.ListingEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class FavoriteRepository(context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val favoriteDao = db.favoriteDao()
    private val listingDao = db.listingDao()

    fun isFavorite(userId: String, listingId: String): Flow<Boolean> {
        return favoriteDao.isFavorite(userId, listingId)
    }

    suspend fun toggleFavorite(userId: String, listingId: String): Boolean {
        val currentlyFav = favoriteDao.isFavorite(userId, listingId).first()
        if (currentlyFav) {
            favoriteDao.removeFavorite(userId, listingId)
            return false
        } else {
            favoriteDao.addFavorite(FavoriteEntity(userId = userId, listingId = listingId))
            return true
        }
    }

    fun observeFavoriteListings(userId: String): Flow<List<ListingEntity>> {
        return listingDao.observeFavoriteListings(userId)
    }
}
