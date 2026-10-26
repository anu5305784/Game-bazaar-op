package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.entities.GameEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class GameRepository(context: Context) {
    private val db = AppDatabase.getInstance(context)
    private val gameDao = db.gameDao()

    fun getAllGames(): Flow<List<GameEntity>> = gameDao.observeAllGames()

    fun getPopularGames(): Flow<List<GameEntity>> = gameDao.observePopularGames()

    fun getGamesByCategory(category: String): Flow<List<GameEntity>> {
        return if (category == "All") {
            gameDao.observeAllGames()
        } else {
            gameDao.observeGamesByCategory(category)
        }
    }

    fun searchGames(query: String): Flow<List<GameEntity>> {
        return if (query.isBlank()) {
            gameDao.observeAllGames()
        } else {
            gameDao.searchGames(query.trim())
        }
    }

    suspend fun getGameById(id: String): GameEntity? = gameDao.getGameById(id)

    suspend fun addGame(
        title: String,
        category: String,
        publisher: String,
        isPopular: Boolean = false
    ): Result<GameEntity> {
        if (title.isBlank()) return Result.failure(IllegalArgumentException("Game title cannot be blank"))
        val id = "g_custom_${UUID.randomUUID().toString().take(8)}"
        val game = GameEntity(
            id = id,
            title = title.trim(),
            category = category.ifBlank { "Mobile Strategy & RPG" },
            publisher = publisher.ifBlank { "Independent Publisher" },
            isPopular = isPopular,
            activeListingsCount = 0
        )
        gameDao.insertGame(game)
        return Result.success(game)
    }

    suspend fun updateGame(game: GameEntity): Result<Unit> {
        gameDao.updateGame(game)
        return Result.success(Unit)
    }

    suspend fun deleteGame(game: GameEntity): Result<Unit> {
        gameDao.deleteGame(game)
        return Result.success(Unit)
    }
}
