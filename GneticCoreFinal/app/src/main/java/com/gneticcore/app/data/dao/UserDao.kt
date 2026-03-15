package com.gneticcore.app.data.dao

import androidx.room.*
import com.gneticcore.app.data.entity.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    suspend fun login(username: String, password: String): User?

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun findByUsername(username: String): User?

    @Query("SELECT * FROM users WHERE displayName = :displayName LIMIT 1")
    suspend fun findByDisplayName(displayName: String): User?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun findById(id: Int): User?

    @Query("SELECT COUNT(*) FROM users")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: User): Long
}
