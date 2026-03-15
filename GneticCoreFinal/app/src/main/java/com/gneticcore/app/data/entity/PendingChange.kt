package com.gneticcore.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_changes")
data class PendingChange(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val changeType: String = ChangeType.NEW_GAME,
    val gameId: Int,
    val requestedByUserId: Int = 0,
    val requestedByDisplayName: String = "",
    // Original values (EDIT_GAME only)
    val originalTitle: String = "",
    val originalPlatform: String = "",
    val originalGenre: String = "",
    val originalReleaseYear: Int = 0,
    val originalRating: Float = 0f,
    val originalDeveloper: String = "",
    val originalImageUri: String = "",
    val originalImageBiasX: Float = 0.5f,
    val originalImageBiasY: Float = 0.5f,
    // Proposed values
    val proposedTitle: String,
    val proposedPlatform: String,
    val proposedGenre: String,
    val proposedReleaseYear: Int,
    val proposedRating: Float,
    val proposedDeveloper: String,
    val proposedImageUri: String = "",
    val proposedImageBiasX: Float = 0.5f,
    val proposedImageBiasY: Float = 0.5f,
    val requestedAt: Long = System.currentTimeMillis()
)

object ChangeType {
    const val NEW_GAME  = "NEW_GAME"
    const val EDIT_GAME = "EDIT_GAME"
}
