package com.gneticcore.app.data.dao

import androidx.room.*
import com.gneticcore.app.data.entity.Comment
import com.gneticcore.app.data.entity.CommentVote
import kotlinx.coroutines.flow.Flow

@Dao
interface CommentDao {
    @Query("SELECT * FROM comments WHERE gameId = :gameId ORDER BY createdAt DESC")
    fun getCommentsForGame(gameId: Int): Flow<List<Comment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(comment: Comment)

    @Delete
    suspend fun delete(comment: Comment)

    // Votes
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVote(vote: CommentVote)

    @Query("DELETE FROM comment_votes WHERE commentId = :commentId AND userId = :userId")
    suspend fun deleteVote(commentId: Int, userId: Int)

    @Query("SELECT * FROM comment_votes WHERE commentId = :commentId AND userId = :userId")
    suspend fun getVote(commentId: Int, userId: Int): CommentVote?

    @Query("SELECT * FROM comment_votes WHERE commentId = :commentId")
    fun getVotesForComment(commentId: Int): Flow<List<CommentVote>>

    @Query("SELECT * FROM comment_votes WHERE commentId IN (SELECT id FROM comments WHERE gameId = :gameId)")
    fun getVotesForGame(gameId: Int): Flow<List<CommentVote>>

    @Transaction
    suspend fun toggleVote(commentId: Int, userId: Int, isLike: Boolean) {
        val existing = getVote(commentId, userId)
        if (existing != null) {
            if (existing.isLike == isLike) {
                deleteVote(commentId, userId)
            } else {
                insertVote(existing.copy(isLike = isLike))
            }
        } else {
            insertVote(CommentVote(commentId = commentId, userId = userId, isLike = isLike))
        }
    }
}
