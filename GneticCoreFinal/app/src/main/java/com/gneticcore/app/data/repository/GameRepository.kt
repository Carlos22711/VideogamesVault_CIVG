package com.gneticcore.app.data.repository

import com.gneticcore.app.data.dao.CommentDao
import com.gneticcore.app.data.dao.GameDao
import com.gneticcore.app.data.dao.PendingChangeDao
import com.gneticcore.app.data.dao.UserDao
import com.gneticcore.app.data.entity.*
import kotlinx.coroutines.flow.Flow

class GameRepository(
    private val userDao: UserDao,
    private val gameDao: GameDao,
    private val pendingChangeDao: PendingChangeDao,
    private val commentDao: CommentDao
) {
    // Users
    suspend fun login(username: String, password: String): User? = userDao.login(username, password)
    suspend fun findByUsername(username: String): User?           = userDao.findByUsername(username)
    suspend fun findByDisplayName(name: String): User?            = userDao.findByDisplayName(name)
    suspend fun registerUser(user: User): Long                    = userDao.insert(user)

    // Games
    val allApproved: Flow<List<Game>>  = gameDao.getAllApproved()
    val topRated: Flow<List<Game>>     = gameDao.getTopRated()
    val recent: Flow<List<Game>>       = gameDao.getRecent()
    val approvedCount: Flow<Int>       = gameDao.getApprovedCount()

    suspend fun insertGame(game: Game): Long = gameDao.insert(game)
    suspend fun updateGame(game: Game)       = gameDao.update(game)
    suspend fun deleteGame(game: Game)       = gameDao.delete(game)
    suspend fun getGameById(id: Int): Game?  = gameDao.getById(id)
    suspend fun getGameByTitleAndPlatform(title: String, platform: String): Game? = gameDao.getByTitleAndPlatform(title, platform)
    fun getGameByIdFlow(id: Int): Flow<Game?> = gameDao.getByIdFlow(id)

    // Ratings
    suspend fun getUserRating(gameId: Int, userId: Int) = gameDao.getUserRating(gameId, userId)
    suspend fun updateGameRating(gameId: Int, userId: Int, rating: Int) = gameDao.updateGameRating(gameId, userId, rating)

    // Pending
    val pendingChanges: Flow<List<PendingChange>> = pendingChangeDao.getAll()
    val pendingCount: Flow<Int>                   = pendingChangeDao.getCount()

    suspend fun insertPending(c: PendingChange)  = pendingChangeDao.insert(c)
    suspend fun deletePending(c: PendingChange)  = pendingChangeDao.delete(c)
    suspend fun deletePendingByGame(id: Int)     = pendingChangeDao.deleteByGameId(id)

    // Comments
    fun getCommentsForGame(gameId: Int): Flow<List<Comment>>  = commentDao.getCommentsForGame(gameId)
    suspend fun insertComment(comment: Comment)               = commentDao.insert(comment)
    suspend fun deleteComment(comment: Comment)               = commentDao.delete(comment)
    
    // Comment Votes
    fun getVotesForComment(commentId: Int) = commentDao.getVotesForComment(commentId)
    fun getVotesForGame(gameId: Int) = commentDao.getVotesForGame(gameId)
    suspend fun toggleCommentVote(commentId: Int, userId: Int, isLike: Boolean) = commentDao.toggleVote(commentId, userId, isLike)
    suspend fun getCommentVote(commentId: Int, userId: Int) = commentDao.getVote(commentId, userId)
}
