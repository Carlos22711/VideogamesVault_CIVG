package com.gneticcore.app.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "games",
    indices = [Index(value = ["title", "platform"], unique = true)]
)
data class Game(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val platform: String,
    val genre: String,
    val releaseYear: Int,
    val rating: Float = 0f,             // This will be the average rating
    val voteCount: Int = 0,              // Total number of votes
    val developer: String,
    val approvalStatus: String = ApprovalStatus.APPROVED,
    val imageUri: String = "",          // local URI from gallery, empty = no image
    val imageBiasX: Float = 0.5f,       // Horizontal alignment (0.0 = left, 1.0 = right)
    val imageBiasY: Float = 0.5f        // Vertical alignment (0.0 = top, 1.0 = bottom)
)

object ApprovalStatus {
    const val APPROVED = "APPROVED"
    const val PENDING  = "PENDING"
}
