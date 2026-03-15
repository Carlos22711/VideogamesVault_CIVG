package com.gneticcore.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gneticcore.app.data.database.GameDatabase
import com.gneticcore.app.data.entity.*
import com.gneticcore.app.data.repository.GameRepository
import com.gneticcore.app.utils.UserSession
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class AuthResult {
    object Success : AuthResult()
    data class Error(val message: String) : AuthResult()
}

data class CommentUI(
    val comment: Comment,
    val votes: List<CommentVote> = emptyList(),
    val replies: List<CommentUI> = emptyList()
)

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val db   = GameDatabase.getInstance(application)
    private val repo = GameRepository(db.userDao(), db.gameDao(), db.pendingChangeDao(), db.commentDao())

    // Juegos
    val games          = repo.allApproved.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val topRatedGames  = repo.topRated.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val recentGames    = repo.recent.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val approvedCount  = repo.approvedCount.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
    val pendingChanges = repo.pendingChanges.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val pendingCount   = repo.pendingCount.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    // Autenticación
    suspend fun login(username: String, password: String): AuthResult {
        val user = repo.login(username.trim(), password)
            ?: return AuthResult.Error("Usuario o contraseña incorrectos")
        UserSession.login(user.id, user.username, user.displayName, user.role)
        return AuthResult.Success
    }

    suspend fun register(username: String, displayName: String, password: String, confirm: String): AuthResult {
        if (password != confirm) return AuthResult.Error("Las contraseñas no coinciden")
        if (repo.findByUsername(username.trim()) != null) return AuthResult.Error("Ese nombre de usuario ya está en uso")
        if (repo.findByDisplayName(displayName.trim()) != null) return AuthResult.Error("Ese identificador ya está en uso")
        repo.registerUser(User(username = username.trim(), displayName = displayName.trim(), password = password, role = UserRole.USER))
        return login(username.trim(), password)
    }

    fun logout() = UserSession.logout()

    // Admin: juegos
    fun insertGame(game: Game) = viewModelScope.launch {
        val newGameId = repo.insertGame(game.copy(approvalStatus = ApprovalStatus.APPROVED)).toInt()
        // También registramos la valoración inicial del administrador
        repo.updateGameRating(newGameId, UserSession.userId, game.rating.toInt())
    }
    fun updateGame(game: Game) = viewModelScope.launch { repo.updateGame(game) }
    fun deleteGame(game: Game) = viewModelScope.launch {
        repo.deleteGame(game)
        repo.deletePendingByGame(game.id)
    }

    // Usuario: proponer nuevo juego
    fun proposeNewGame(game: Game) = viewModelScope.launch {
        repo.insertPending(PendingChange(
            changeType             = ChangeType.NEW_GAME,
            gameId                 = 0,
            requestedByUserId      = UserSession.userId,
            requestedByDisplayName = UserSession.displayName,
            proposedTitle          = game.title,
            proposedPlatform       = game.platform,
            proposedGenre          = game.genre,
            proposedReleaseYear    = game.releaseYear,
            proposedRating         = game.rating,
            proposedDeveloper      = game.developer,
            proposedImageUri       = game.imageUri,
            proposedImageBiasX     = game.imageBiasX,
            proposedImageBiasY     = game.imageBiasY
        ))
    }

    // Usuario: proponer edición
    fun proposePendingChange(change: PendingChange) = viewModelScope.launch {
        repo.insertPending(change.copy(
            requestedByUserId = UserSession.userId,
            requestedByDisplayName = UserSession.displayName
        ))
    }

    // Admin: aprobar
    fun approvePendingChange(change: PendingChange) = viewModelScope.launch {
        when (change.changeType) {
            ChangeType.NEW_GAME -> {
                // Verificar si ya existe para evitar duplicados (aunque la UI debería prevenirlo)
                val existing = repo.getGameByTitleAndPlatform(change.proposedTitle, change.proposedPlatform)
                if (existing != null) {
                    repo.deletePending(change)
                    return@launch
                }

                // 1. Insertar el juego
                val newGameId = repo.insertGame(Game(
                    title = change.proposedTitle, platform = change.proposedPlatform,
                    genre = change.proposedGenre, releaseYear = change.proposedReleaseYear,
                    rating = change.proposedRating, developer = change.proposedDeveloper,
                    approvalStatus = ApprovalStatus.APPROVED,
                    imageUri = change.proposedImageUri,
                    imageBiasX = change.proposedImageBiasX,
                    imageBiasY = change.proposedImageBiasY
                )).toInt()
                
                // 2. Insertar la valoración inicial del proponente (esto solo ocurre en juegos NUEVOS)
                repo.updateGameRating(newGameId, change.requestedByUserId, change.proposedRating.toInt())
            }
            ChangeType.EDIT_GAME -> {
                val orig = repo.getGameById(change.gameId) ?: return@launch
                // Solo actualizamos los metadatos. NO tocamos las estrellas ni el contador de reseñas.
                repo.updateGame(orig.copy(
                    title = change.proposedTitle, platform = change.proposedPlatform,
                    genre = change.proposedGenre, releaseYear = change.proposedReleaseYear,
                    developer = change.proposedDeveloper,
                    imageUri = change.proposedImageUri,
                    imageBiasX = change.proposedImageBiasX,
                    imageBiasY = change.proposedImageBiasY
                ))
            }
        }
        repo.deletePending(change)
    }

    fun rejectPendingChange(change: PendingChange) = viewModelScope.launch { repo.deletePending(change) }

    // Flujo optimizado de UI de comentarios
    fun getCommentsUI(gameId: Int): Flow<List<CommentUI>> {
        return combine(
            repo.getCommentsForGame(gameId),
            repo.getVotesForGame(gameId)
        ) { comments, allVotes ->
            val votesMap = allVotes.groupBy { it.commentId }
            val mainComments = comments.filter { it.parentCommentId == null }
            val replies = comments.filter { it.parentCommentId != null }
            
            mainComments.map { main ->
                CommentUI(
                    comment = main,
                    votes = votesMap[main.id] ?: emptyList(),
                    replies = replies.filter { it.parentCommentId == main.id }.map { reply ->
                        CommentUI(
                            comment = reply,
                            votes = votesMap[reply.id] ?: emptyList()
                        )
                    }
                )
            }
        }.flowOn(kotlinx.coroutines.Dispatchers.Default)
    }

    fun addComment(gameId: Int, text: String, parentCommentId: Int? = null) = viewModelScope.launch {
        repo.insertComment(Comment(
            gameId          = gameId,
            userId          = UserSession.userId,
            displayName     = UserSession.displayName,
            text            = text.trim(),
            parentCommentId = parentCommentId
        ))
    }
    fun deleteComment(comment: Comment) = viewModelScope.launch { repo.deleteComment(comment) }
    
    // Votos de comentarios
    fun toggleCommentVote(commentId: Int, isLike: Boolean) = viewModelScope.launch {
        repo.toggleCommentVote(commentId, UserSession.userId, isLike)
    }
    
    // Valoración
    fun getGameByIdFlow(id: Int) = repo.getGameByIdFlow(id)
    
    private val _userRating = MutableStateFlow<Int?>(null)
    val userRating = _userRating.asStateFlow()
    
    fun loadUserRating(gameId: Int) = viewModelScope.launch {
        _userRating.value = repo.getUserRating(gameId, UserSession.userId)?.rating
    }
    
    fun updateRating(gameId: Int, rating: Int) = viewModelScope.launch {
        repo.updateGameRating(gameId, UserSession.userId, rating)
        _userRating.value = rating
    }

    suspend fun getGameById(id: Int): Game? = repo.getGameById(id)
    suspend fun checkDuplicate(title: String, platform: String): Game? = repo.getGameByTitleAndPlatform(title, platform)
}
