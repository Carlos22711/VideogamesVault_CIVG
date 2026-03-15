package com.gneticcore.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "comment_votes")
data class CommentVote(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val commentId: Int,
    val userId: Int,
    val isLike: Boolean // true for like, false for dislike
)
