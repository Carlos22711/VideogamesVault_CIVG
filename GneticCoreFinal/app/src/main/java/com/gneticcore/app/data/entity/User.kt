package com.gneticcore.app.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["username"], unique = true),
        Index(value = ["displayName"], unique = true)
    ]
)
data class User(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val username: String,
    val displayName: String,
    val password: String,
    val role: String = UserRole.USER
)

object UserRole {
    const val ADMIN = "ADMIN"
    const val USER  = "USER"
}
