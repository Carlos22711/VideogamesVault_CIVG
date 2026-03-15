package com.gneticcore.app.ui.screens

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.gneticcore.app.R
import com.gneticcore.app.data.entity.ChangeType
import com.gneticcore.app.data.entity.PendingChange
import com.gneticcore.app.ui.viewmodel.GameViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingListScreen(viewModel: GameViewModel, onBack: () -> Unit) {
    val pendingChanges by viewModel.pendingChanges.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Color(0xFF0A0A0F),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            stringResource(R.string.section_pending_approvals),
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE8E8F0)
                        )
                        if (pendingChanges.isNotEmpty()) {
                            Spacer(Modifier.width(8.dp))
                            Badge { Text("${pendingChanges.size}") }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color(0xFFB0B0C8))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF13131A))
            )
        }
    ) { padding ->
        if (pendingChanges.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFF0A0A0F)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF00E676), modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(16.dp))
                    Text(stringResource(R.string.msg_no_pending), color = Color(0xFFB0B0C8), fontSize = 16.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFF0A0A0F))
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(pendingChanges, key = { it.id }) { change ->
                    PendingChangeCard(
                        change    = change,
                        onApprove = { viewModel.approvePendingChange(change) },
                        onReject  = { viewModel.rejectPendingChange(change) }
                    )
                }
            }
        }
    }
}

@Composable
fun PendingChangeCard(
    change: PendingChange,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val isNewGame = change.changeType == ChangeType.NEW_GAME
    val dateStr = remember(change.requestedAt) {
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(change.requestedAt))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF13131A)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Header: tipo + autor + fecha
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isNewGame) Color(0xFF388E3C).copy(alpha = 0.2f)
                            else Color(0xFF0070CC).copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (isNewGame) Icons.Default.AddCircle else Icons.Default.Edit,
                            contentDescription = null,
                            tint = if (isNewGame) Color(0xFF66BB6A) else Color(0xFF29B6F6),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            if (isNewGame) stringResource(R.string.change_new_game)
                            else stringResource(R.string.change_edit_game),
                            fontSize = 11.sp,
                            color = if (isNewGame) Color(0xFF66BB6A) else Color(0xFF29B6F6),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                // Identifier of who proposed it
                if (change.requestedByDisplayName.isNotBlank()) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF666680), modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(2.dp))
                    Text(change.requestedByDisplayName, color = Color(0xFF888899), fontSize = 11.sp)
                }
                Spacer(Modifier.weight(1f))
                Text(dateStr, color = Color(0xFF555570), fontSize = 11.sp)
            }

            Spacer(Modifier.height(12.dp))

            if (isNewGame) {
                NewGameDetails(change)
            } else {
                EditGameDiff(change)
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFF2A2A3E))
            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = onReject,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252))
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.btn_reject))
                }
                Button(
                    onClick = onApprove,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.btn_approve))
                }
            }
        }
    }
}

@Composable
fun NewGameDetails(change: PendingChange) {
    if (change.proposedImageUri.isNotBlank()) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(Uri.parse(change.proposedImageUri))
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alignment = BiasAlignment(
                horizontalBias = (change.proposedImageBiasX * 2f) - 1f,
                verticalBias = (change.proposedImageBiasY * 2f) - 1f
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(bottom = 12.dp)
                .clip(RoundedCornerShape(8.dp))
        )
    }

    Text(change.proposedTitle, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE8E8F0))
    Spacer(Modifier.height(6.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Chip(change.proposedPlatform, com.gneticcore.app.utils.GameConstants.getPlatformColor(change.proposedPlatform))
        Chip(change.proposedGenre, com.gneticcore.app.utils.GameConstants.getGenreColor(change.proposedGenre))
        Chip("${change.proposedReleaseYear}", Color(0xFF546E7A))
    }
    Spacer(Modifier.height(4.dp))
    Text(stringResource(R.string.label_developer_prefix, change.proposedDeveloper), color = Color(0xFFB0B0C8), fontSize = 13.sp)
}

@Composable
fun EditGameDiff(change: PendingChange) {
    // Show image change if URI or Bias changed
    val imageChanged = change.originalImageUri != change.proposedImageUri ||
                       change.originalImageBiasX != change.proposedImageBiasX ||
                       change.originalImageBiasY != change.proposedImageBiasY

    if (imageChanged) {
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (change.originalImageUri.isNotBlank()) {
                AsyncImage(
                    model = Uri.parse(change.originalImageUri),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    alignment = BiasAlignment((change.originalImageBiasX * 2f) - 1f, (change.originalImageBiasY * 2f) - 1f),
                    modifier = Modifier.weight(1f).height(80.dp).clip(RoundedCornerShape(6.dp)).background(Color.Black)
                )
            } else {
                Box(Modifier.weight(1f).height(80.dp).background(Color(0xFF1E1E2E), RoundedCornerShape(6.dp)), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.label_no_image), color = Color(0xFF444460), fontSize = 10.sp)
                }
            }
            Icon(Icons.Default.ArrowForward, null, tint = Color(0xFF7C4DFF), modifier = Modifier.align(Alignment.CenterVertically).size(20.dp))
            if (change.proposedImageUri.isNotBlank()) {
                AsyncImage(
                    model = Uri.parse(change.proposedImageUri),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    alignment = BiasAlignment((change.proposedImageBiasX * 2f) - 1f, (change.proposedImageBiasY * 2f) - 1f),
                    modifier = Modifier.weight(1f).height(80.dp).clip(RoundedCornerShape(6.dp)).background(Color.Black)
                )
            } else {
                Box(Modifier.weight(1f).height(80.dp).background(Color(0xFF1E1E2E), RoundedCornerShape(6.dp)), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.btn_delete), color = Color(0xFFFF5252), fontSize = 10.sp)
                }
            }
        }
    }

    DiffField(label = stringResource(R.string.field_title),        original = change.originalTitle,                    proposed = change.proposedTitle,                    changed = change.originalTitle != change.proposedTitle)
    DiffField(label = stringResource(R.string.field_platform),    original = change.originalPlatform,                 proposed = change.proposedPlatform,                 changed = change.originalPlatform != change.proposedPlatform)
    DiffField(label = stringResource(R.string.field_genre),        original = change.originalGenre,                    proposed = change.proposedGenre,                    changed = change.originalGenre != change.proposedGenre)
    DiffField(label = stringResource(R.string.field_year),           original = "${change.originalReleaseYear}",         proposed = "${change.proposedReleaseYear}",         changed = change.originalReleaseYear != change.proposedReleaseYear)
    DiffField(label = stringResource(R.string.field_developer),original = change.originalDeveloper,                proposed = change.proposedDeveloper,                changed = change.originalDeveloper != change.proposedDeveloper)
}

@Composable
fun DiffField(label: String, original: String, proposed: String, changed: Boolean) {
    if (!changed) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("$label: ", color = Color(0xFF444460), fontSize = 12.sp, modifier = Modifier.width(100.dp))
            Text(proposed, color = Color(0xFF555570), fontSize = 12.sp)
        }
    } else {
        DiffRow(label = label, changed = true) {
            Text(
                original,
                color = Color(0xFFFF5252).copy(alpha = 0.8f),
                fontSize = 13.sp,
                textDecoration = TextDecoration.LineThrough
            )
            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color(0xFF7C4DFF),
                modifier = Modifier.size(14.dp).padding(horizontal = 2.dp))
            Text(proposed, color = Color(0xFF66BB6A), fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DiffRow(label: String, changed: Boolean, content: @Composable RowScope.() -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .then(
                if (changed) Modifier
                    .background(Color(0xFF7C4DFF).copy(alpha = 0.07f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                else Modifier
            ),
        verticalAlignment = Alignment.CenterVertically) {
        Text(
            "$label: ",
            color = if (changed) Color(0xFFB0B0C8) else Color(0xFF444460),
            fontSize = 12.sp,
            modifier = Modifier.width(100.dp)
        )
        content()
    }
}

@Composable
fun StarRow(rating: Float, strikethrough: Boolean = false, dimmed: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        repeat(5) { i ->
            Icon(
                Icons.Default.Star,
                contentDescription = null,
                tint = if (i < rating) {
                    if (dimmed) Color(0xFFFFD700).copy(alpha = 0.4f) else Color(0xFFFFD700)
                } else Color(0xFF3D3D5C),
                modifier = Modifier.size(14.dp)
            )
        }
        if (strikethrough) {
            Text(
                " ${rating.toInt()}/5",
                fontSize = 11.sp,
                color = Color(0xFFFF5252).copy(alpha = 0.6f),
                textDecoration = TextDecoration.LineThrough
            )
        }
    }
}
