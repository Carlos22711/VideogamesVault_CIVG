package com.gneticcore.app.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.gneticcore.app.R
import com.gneticcore.app.data.entity.Comment
import com.gneticcore.app.data.entity.CommentVote
import com.gneticcore.app.data.entity.Game
import com.gneticcore.app.ui.viewmodel.CommentUI
import com.gneticcore.app.ui.viewmodel.GameViewModel
import com.gneticcore.app.utils.GameConstants
import com.gneticcore.app.utils.UserSession
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameDetailScreen(
    viewModel: GameViewModel,
    gameId: Int,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onProposeEdit: () -> Unit
) {
    val game by viewModel.getGameByIdFlow(gameId).collectAsStateWithLifecycle(initialValue = null)
    val commentsUI by remember(gameId) { viewModel.getCommentsUI(gameId) }.collectAsStateWithLifecycle(initialValue = emptyList())
    val userRating by viewModel.userRating.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    var commentText by remember { mutableStateOf("") }
    var replyToComment by remember { mutableStateOf<Comment?>(null) }
    var commentError by remember { mutableStateOf<String?>(null) }
    val isAdmin = UserSession.isAdmin

    LaunchedEffect(gameId) {
        viewModel.loadUserRating(gameId)
    }

    val g = game ?: return

    val platformColor = GameConstants.getPlatformColor(g.platform)
    val genreColor    = GameConstants.getGenreColor(g.genre)

    Scaffold(
        containerColor = Color(0xFF0A0A0F),
        topBar = {
            TopAppBar(
                title = { Text(g.title, fontWeight = FontWeight.Bold, color = Color(0xFFE8E8F0), maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color(0xFFB0B0C8))
                    }
                },
                actions = {
                    if (isAdmin) {
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.btn_edit), tint = Color(0xFF00E5FF))
                        }
                    } else {
                        IconButton(onClick = onProposeEdit) {
                            Icon(Icons.Default.EditNote, contentDescription = stringResource(R.string.btn_propose), tint = Color(0xFF00E5FF))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF13131A))
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFF0A0A0F)),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // ── Imagen heroica o banner de color ──
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    if (g.imageUri.isNotBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(Uri.parse(g.imageUri))
                                .crossfade(true)
                                .build(),
                            contentDescription = g.title,
                            contentScale = ContentScale.Crop,
                            alignment = BiasAlignment(
                                horizontalBias = (g.imageBiasX * 2f) - 1f,
                                verticalBias = (g.imageBiasY * 2f) - 1f
                            ),
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(listOf(platformColor.copy(alpha = 0.6f), Color(0xFF0A0A0F)))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(GameConstants.getPlatformEmoji(g.platform), fontSize = 72.sp)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color(0xFF0A0A0F)),
                                    startY = 300f
                                )
                            )
                    )
                    // Insignia de calificación global (Solo lectura)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF13131A).copy(alpha = 0.9f),
                        modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            val formattedRating = String.format(Locale.getDefault(), "%.1f", g.rating)
                            Text("$formattedRating (${g.voteCount})", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }

            // ── Título + etiquetas (chips) ──
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(g.title, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE8E8F0))
                    Spacer(Modifier.height(4.dp))
                    Text(g.developer, fontSize = 14.sp, color = Color(0xFFB0B0C8))
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Chip(g.platform, platformColor)
                        Chip(g.genre, genreColor)
                        Chip("${g.releaseYear}", Color(0xFF546E7A))
                    }
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFF1E1E2E))
                }
            }

            // ── Sección de Tu Calificación ──
            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.section_your_rating), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE8E8F0))
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        (1..5).forEach { star ->
                            val isSelected = userRating != null && star <= userRating!!
                            IconButton(
                                onClick = { 
                                    viewModel.updateRating(g.id, star)
                                },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    if (isSelected) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = null,
                                    tint = if (isSelected) Color(0xFFFFD700) else Color(0xFF3D3D5C),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        if (userRating != null) {
                            Text("$userRating/5", color = Color(0xFFB0B0C8), modifier = Modifier.align(Alignment.CenterVertically))
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFF1E1E2E))
                }
            }

            // ── Encabezado de comentarios ──
            item {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Comment, null, tint = Color(0xFF7C4DFF), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.section_comments, commentsUI.size),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE8E8F0)
                    )
                }
            }

            // ── Entrada para añadir comentario ──
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    if (replyToComment != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                .background(Color(0xFF7C4DFF).copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                .padding(8.dp)
                        ) {
                            Icon(Icons.Default.Reply, null, tint = Color(0xFF7C4DFF), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                stringResource(R.string.msg_replying_to, replyToComment?.displayName ?: ""),
                                color = Color(0xFFE8E8F0), fontSize = 12.sp, modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { replyToComment = null }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, null, tint = Color(0xFFB0B0C8), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it; commentError = null },
                        label = { Text(if (replyToComment == null) stringResource(R.string.msg_comment_placeholder) else stringResource(R.string.msg_reply_placeholder), color = Color(0xFFB0B0C8)) },
                        isError = commentError != null,
                        supportingText = commentError?.let { { Text(it, color = Color(0xFFFF5252)) } },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4,
                        colors = formFieldColors()
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Button(
                            onClick = {
                                if (commentText.isBlank()) {
                                    commentError = context.getString(R.string.error_comment_empty)
                                    return@Button
                                }
                                viewModel.addComment(gameId, commentText, replyToComment?.id)
                                commentText = ""
                                replyToComment = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF))
                        ) {
                            Icon(Icons.Default.Send, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(if (replyToComment == null) stringResource(R.string.btn_publish) else stringResource(R.string.btn_reply))
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    HorizontalDivider(color = Color(0xFF1E1E2E))
                }
            }

            // ── Lista de comentarios ──
            if (commentsUI.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(stringResource(R.string.msg_no_comments), color = Color(0xFF555570), fontSize = 14.sp)
                    }
                }
            } else {
                commentsUI.forEach { mainCommentUI ->
                    item(key = mainCommentUI.comment.id) {
                        CommentCard(
                            comment   = mainCommentUI.comment,
                            votes     = mainCommentUI.votes,
                            canDelete = isAdmin || mainCommentUI.comment.userId == UserSession.userId,
                            onDelete  = { viewModel.deleteComment(mainCommentUI.comment) },
                            onVote    = { isLike -> viewModel.toggleCommentVote(mainCommentUI.comment.id, isLike) },
                            onReply   = { replyToComment = mainCommentUI.comment }
                        )
                    }
                    items(mainCommentUI.replies, key = { it.comment.id }) { replyUI ->
                        Box(modifier = Modifier.padding(start = 40.dp)) {
                            CommentCard(
                                comment   = replyUI.comment,
                                votes     = replyUI.votes,
                                canDelete = isAdmin || replyUI.comment.userId == UserSession.userId,
                                onDelete  = { viewModel.deleteComment(replyUI.comment) },
                                onVote    = { isLike -> viewModel.toggleCommentVote(replyUI.comment.id, isLike) },
                                onReply   = null
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CommentCard(
    comment: Comment,
    votes: List<CommentVote>,
    canDelete: Boolean,
    onDelete: () -> Unit,
    onVote: (Boolean) -> Unit,
    onReply: (() -> Unit)? = null
) {
    val dateStr = remember(comment.createdAt) {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(comment.createdAt))
    }
    val initials = comment.displayName.take(2).uppercase()
    
    val likes = votes.count { it.isLike }
    val dislikes = votes.count { !it.isLike }
    val userVote = votes.find { it.userId == UserSession.userId }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(if (onReply != null) 36.dp else 28.dp)
                .clip(CircleShape)
                .background(Color(0xFF4527A0)),
            contentAlignment = Alignment.Center
        ) {
            Text(initials, color = Color.White, fontSize = if (onReply != null) 13.sp else 11.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(comment.displayName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF7C4DFF))
                Spacer(Modifier.width(8.dp))
                Text(dateStr, fontSize = 11.sp, color = Color(0xFF555570))
            }
            Spacer(Modifier.height(4.dp))
            Text(comment.text, fontSize = 14.sp, color = Color(0xFFD0D0E8), lineHeight = 20.sp)
            
            Spacer(Modifier.height(8.dp))
            // Botones de acción (Me gusta, No me gusta, Responder)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onVote(true) }, modifier = Modifier.size(24.dp)) {
                    Icon(
                        if (userVote?.isLike == true) Icons.Default.ThumbUp else Icons.Outlined.ThumbUp,
                        contentDescription = "Me gusta",
                        tint = if (userVote?.isLike == true) Color(0xFF00E676) else Color(0xFF555570),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text("$likes", color = Color(0xFF555570), fontSize = 12.sp, modifier = Modifier.padding(horizontal = 4.dp))
                
                Spacer(Modifier.width(8.dp))
                
                IconButton(onClick = { onVote(false) }, modifier = Modifier.size(24.dp)) {
                    Icon(
                        if (userVote?.isLike == false) Icons.Default.ThumbDown else Icons.Outlined.ThumbDown,
                        contentDescription = "No me gusta",
                        tint = if (userVote?.isLike == false) Color(0xFFFF5252) else Color(0xFF555570),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text("$dislikes", color = Color(0xFF555570), fontSize = 12.sp, modifier = Modifier.padding(horizontal = 4.dp))

                if (onReply != null) {
                    Spacer(Modifier.width(16.dp))
                    TextButton(
                        onClick = onReply,
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.height(24.dp)
                    ) {
                        Text(stringResource(R.string.btn_reply), color = Color(0xFF7C4DFF), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
        if (canDelete) {
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.DeleteOutline, null, tint = Color(0xFF555570), modifier = Modifier.size(18.dp))
            }
        }
    }
}
