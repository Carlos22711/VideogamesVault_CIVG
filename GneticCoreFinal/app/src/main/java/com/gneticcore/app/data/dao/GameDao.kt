package com.gneticcore.app.data.dao

import androidx.room.*
import com.gneticcore.app.data.entity.Game
import com.gneticcore.app.data.entity.GameRating
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM games WHERE approvalStatus = 'APPROVED' ORDER BY title ASC")
    fun getAllApproved(): Flow<List<Game>>

    // "Más Valorados" ahora funciona por cantidad de reseñas (voteCount)
    @Query("SELECT * FROM games WHERE approvalStatus = 'APPROVED' ORDER BY voteCount DESC")
    fun getTopRated(): Flow<List<Game>>

    // Restauramos "Más recientes"
    @Query("SELECT * FROM games WHERE approvalStatus = 'APPROVED' ORDER BY id DESC")
    fun getRecent(): Flow<List<Game>>

    @Query("SELECT COUNT(*) FROM games WHERE approvalStatus = 'APPROVED'")
    fun getApprovedCount(): Flow<Int>

    @Query("SELECT * FROM games WHERE id = :id")
    suspend fun getById(id: Int): Game?
    
    @Query("SELECT * FROM games WHERE title = :title AND platform = :platform")
    suspend fun getByTitleAndPlatform(title: String, platform: String): Game?

    @Query("SELECT * FROM games WHERE id = :id")
    fun getByIdFlow(id: Int): Flow<Game?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(game: Game): Long

    @Update
    suspend fun update(game: Game)

    @Delete
    suspend fun delete(game: Game)

    // Rating related
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRating(rating: GameRating)

    @Query("SELECT * FROM game_ratings WHERE gameId = :gameId AND userId = :userId")
    suspend fun getUserRating(gameId: Int, userId: Int): GameRating?

    @Query("SELECT * FROM game_ratings WHERE gameId = :gameId")
    fun getRatingsForGame(gameId: Int): Flow<List<GameRating>>
    
    @Transaction
    suspend fun updateGameRating(gameId: Int, userId: Int, ratingValue: Int) {
        val existing = getUserRating(gameId, userId)
        if (existing != null) {
            insertRating(existing.copy(rating = ratingValue))
        } else {
            insertRating(GameRating(gameId = gameId, userId = userId, rating = ratingValue))
        }
        
        // Recalculate average
        val ratings = getRatingsForGameList(gameId)
        val avg = if (ratings.isEmpty()) 0f else ratings.sumOf { it.rating }.toFloat() / ratings.size
        val game = getById(gameId)
        game?.let {
            update(it.copy(rating = avg, voteCount = ratings.size))
        }
    }
    
    @Query("SELECT * FROM game_ratings WHERE gameId = :gameId")
    suspend fun getRatingsForGameList(gameId: Int): List<GameRating>
}
