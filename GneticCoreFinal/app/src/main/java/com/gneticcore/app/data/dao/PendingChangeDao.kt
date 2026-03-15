package com.gneticcore.app.data.dao

import androidx.room.*
import com.gneticcore.app.data.entity.PendingChange
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingChangeDao {
    @Query("SELECT * FROM pending_changes ORDER BY requestedAt DESC")
    fun getAll(): Flow<List<PendingChange>>

    @Query("SELECT COUNT(*) FROM pending_changes")
    fun getCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(change: PendingChange)

    @Delete
    suspend fun delete(change: PendingChange)

    @Query("DELETE FROM pending_changes WHERE gameId = :gameId")
    suspend fun deleteByGameId(gameId: Int)
}
