package com.gneticcore.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "comments")
data class Comment(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val gameId: Int,
    val userId: Int,
    val displayName: String,
    val text: String,
    val parentCommentId: Int? = null, // Para comentarios anidados
    val createdAt: Long = System.currentTimeMillis()
)
