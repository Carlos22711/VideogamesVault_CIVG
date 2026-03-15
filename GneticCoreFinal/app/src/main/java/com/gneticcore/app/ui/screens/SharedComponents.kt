package com.gneticcore.app.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.gneticcore.app.R
import com.gneticcore.app.data.entity.Game
import com.gneticcore.app.utils.GameConstants
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun authFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor   = Color(0xFF7C4DFF),
    unfocusedBorderColor = Color(0xFF3D3D5C),
    focusedTextColor     = Color(0xFFE8E8F0),
    unfocusedTextColor   = Color(0xFFE8E8F0),
    cursorColor          = Color(0xFF7C4DFF),
    errorBorderColor     = Color(0xFFFF5252),
    focusedLabelColor    = Color(0xFF7C4DFF)
)

@Composable
fun GameCard(
    game: Game,
    onClick: (() -> Unit)? = null,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    onProposeEdit: (() -> Unit)? = null,
    isPending: Boolean = false
) {
    val platformColor = GameConstants.getPlatformColor(game.platform)
    val genreColor    = GameConstants.getGenreColor(game.genre)
    val emoji         = GameConstants.getPlatformEmoji(game.platform)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF13131A)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column {
            // Unificamos altura a 200.dp para coincidir con el selector
            if (game.imageUri.isNotBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(Uri.parse(game.imageUri))
                        .crossfade(true)
                        .build(),
                    contentDescription = game.title,
                    contentScale = ContentScale.Crop,
                    alignment = BiasAlignment(
                        horizontalBias = (game.imageBiasX * 2f) - 1f,
                        verticalBias = (game.imageBiasY * 2f) - 1f
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(Brush.horizontalGradient(listOf(platformColor, genreColor)))
                )
            }

            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (game.imageUri.isBlank()) {
                        Text(emoji, fontSize = 26.sp)
                        Spacer(Modifier.width(10.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(game.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE8E8F0))
                        Text(game.developer, fontSize = 12.sp, color = Color(0xFFB0B0C8))
                    }
                    if (isPending) {
                        Surface(shape = RoundedCornerShape(6.dp), color = Color(0xFFFF6D00).copy(alpha = 0.2f)) {
                            Text(stringResource(R.string.label_pending_caps), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), fontSize = 10.sp, color = Color(0xFFFF6D00), fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Chip(game.platform, platformColor)
                    Chip(game.genre, genreColor)
                    Chip(game.releaseYear.toString(), Color(0xFF546E7A))
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        String.format(Locale.getDefault(), "%.1f", game.rating),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFD700)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("(${game.voteCount})", fontSize = 12.sp, color = Color(0xFFB0B0C8))
                }
                if (onEdit != null || onDelete != null || onProposeEdit != null) {
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                        onProposeEdit?.let {
                            TextButton(onClick = it) { Text("✏️ ${stringResource(R.string.btn_propose)}", color = Color(0xFF00E5FF), fontSize = 12.sp) }
                        }
                        onEdit?.let {
                            TextButton(onClick = it) { Text("✏️ ${stringResource(R.string.btn_edit)}", color = Color(0xFF00E5FF), fontSize = 12.sp) }
                        }
                        onDelete?.let {
                            TextButton(onClick = it) { Text("🗑 ${stringResource(R.string.btn_delete)}", color = Color(0xFFFF5252), fontSize = 12.sp) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Chip(text: String, color: Color) {
    Surface(shape = RoundedCornerShape(6.dp), color = color.copy(alpha = 0.15f)) {
        Text(text, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            fontSize = 11.sp, color = color, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7C4DFF),
        modifier = Modifier.padding(vertical = 8.dp))
}
