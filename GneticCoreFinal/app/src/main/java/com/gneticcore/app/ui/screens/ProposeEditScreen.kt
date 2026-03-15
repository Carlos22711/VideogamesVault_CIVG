package com.gneticcore.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gneticcore.app.R
import com.gneticcore.app.data.entity.ChangeType
import com.gneticcore.app.data.entity.PendingChange
import com.gneticcore.app.ui.viewmodel.GameViewModel
import com.gneticcore.app.utils.GameConstants
import com.gneticcore.app.utils.UserSession
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProposeEditScreen(
    viewModel: GameViewModel,
    gameId: Int?,
    onBack: () -> Unit
) {
    val scope             = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context           = LocalContext.current
    var isSaving          by remember { mutableStateOf(false) }

    var title     by remember { mutableStateOf("") }
    var platform  by remember { mutableStateOf("") }
    var genre     by remember { mutableStateOf("") }
    var yearStr   by remember { mutableStateOf("") }
    var developer by remember { mutableStateOf("") }
    var rating    by remember { mutableStateOf(0f) }
    var imageUri  by remember { mutableStateOf("") }
    var imageBiasX by remember { mutableStateOf(0.5f) }
    var imageBiasY by remember { mutableStateOf(0.5f) }

    // Original snapshot
    var origTitle     by remember { mutableStateOf("") }
    var origPlatform  by remember { mutableStateOf("") }
    var origGenre     by remember { mutableStateOf("") }
    var origYear      by remember { mutableStateOf(0) }
    var origRating    by remember { mutableStateOf(0f) }
    var origDeveloper by remember { mutableStateOf("") }
    var origImageUri  by remember { mutableStateOf("") }
    var origImageBiasX by remember { mutableStateOf(0.5f) }
    var origImageBiasY by remember { mutableStateOf(0.5f) }

    LaunchedEffect(gameId) {
        if (gameId != null && gameId > 0) {
            viewModel.getGameById(gameId)?.let { g ->
                title     = g.title;     platform  = g.platform; genre     = g.genre
                yearStr   = g.releaseYear.toString(); developer = g.developer
                rating    = g.rating;
                imageUri  = g.imageUri;  imageBiasX = g.imageBiasX; imageBiasY = g.imageBiasY
                origTitle = g.title;     origPlatform = g.platform; origGenre = g.genre
                origYear  = g.releaseYear; origDeveloper = g.developer
                origRating = g.rating;
                origImageUri = g.imageUri; origImageBiasX = g.imageBiasX; origImageBiasY = g.imageBiasY
            }
        }
    }

    var titleError    by remember { mutableStateOf<String?>(null) }
    var platformError by remember { mutableStateOf<String?>(null) }
    var genreError    by remember { mutableStateOf<String?>(null) }
    var yearError     by remember { mutableStateOf<String?>(null) }
    var devError      by remember { mutableStateOf<String?>(null) }
    var platformExpanded by remember { mutableStateOf(false) }
    var genreExpanded    by remember { mutableStateOf(false) }

    fun validate(): Boolean {
        var ok = true
        titleError    = if (title.isBlank()) { ok=false; context.getString(R.string.error_required) } else if (title.length<2) { ok=false; context.getString(R.string.error_min_chars) } else null
        platformError = if (platform.isBlank()) { ok=false; context.getString(R.string.hint_select_platform) } else null
        genreError    = if (genre.isBlank()) { ok=false; context.getString(R.string.hint_select_genre) } else null
        yearError = when {
            yearStr.isBlank()                -> { ok=false; context.getString(R.string.error_required) }
            yearStr.toIntOrNull() == null    -> { ok=false; context.getString(R.string.error_year_invalid) }
            yearStr.toInt() !in GameConstants.MIN_YEAR..GameConstants.MAX_YEAR -> { ok=false; context.getString(R.string.error_year_range, GameConstants.MIN_YEAR, GameConstants.MAX_YEAR) }
            else -> null
        }
        devError    = if (developer.isBlank()) { ok=false; context.getString(R.string.error_required) } else if (developer.length<2) { ok=false; context.getString(R.string.error_min_chars) } else null
        return ok
    }

    Scaffold(
        containerColor = Color(0xFF0A0A0F),
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.section_propose_edit), fontWeight = FontWeight.Bold, color = Color(0xFFE8E8F0)) },
                navigationIcon = {
                    IconButton(onClick = onBack, enabled = !isSaving) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = if (isSaving) Color.Gray else Color(0xFFB0B0C8))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF13131A))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFF0A0A0F))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Info banner
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = Color(0xFFFF6D00).copy(alpha = 0.12f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFFF6D00), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.msg_review_notice),
                        color = Color(0xFFFF6D00), fontSize = 13.sp
                    )
                }
            }

            // Propone como: identity chip
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.label_proposing_as), color = Color(0xFFB0B0C8), fontSize = 13.sp)
                Spacer(Modifier.width(4.dp))
                Surface(shape = MaterialTheme.shapes.small, color = Color(0xFF1E1E2E)) {
                    Row(Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, null, tint = Color(0xFF00E5FF), modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(UserSession.displayName, color = Color(0xFF00E5FF), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            FormField(value = title, onValueChange = { title = it; titleError = null }, label = stringResource(R.string.field_title), error = titleError, icon = Icons.Default.Games, enabled = !isSaving)

            ExposedDropdownMenuBox(expanded = platformExpanded && !isSaving, onExpandedChange = { platformExpanded = it }) {
                OutlinedTextField(value = platform, onValueChange = {}, readOnly = true, enabled = !isSaving,
                    label = { Text(stringResource(R.string.field_platform), color = Color(0xFFB0B0C8)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(platformExpanded) },
                    isError = platformError != null,
                    modifier = Modifier.fillMaxWidth().menuAnchor(), colors = formFieldColors())
                ExposedDropdownMenu(expanded = platformExpanded, onDismissRequest = { platformExpanded = false }, modifier = Modifier.background(Color(0xFF1E1E2E))) {
                    GameConstants.PLATFORMS.forEach { p ->
                        DropdownMenuItem(text = { Text("${GameConstants.getPlatformEmoji(p)} $p", color = Color(0xFFE8E8F0)) },
                            onClick = { platform = p; platformError = null; platformExpanded = false })
                    }
                }
            }
            platformError?.let { Text(it, color = Color(0xFFFF5252), fontSize = 12.sp) }

            ExposedDropdownMenuBox(expanded = genreExpanded && !isSaving, onExpandedChange = { genreExpanded = it }) {
                OutlinedTextField(value = genre, onValueChange = {}, readOnly = true, enabled = !isSaving,
                    label = { Text(stringResource(R.string.field_genre), color = Color(0xFFB0B0C8)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(genreExpanded) },
                    isError = genreError != null,
                    modifier = Modifier.fillMaxWidth().menuAnchor(), colors = formFieldColors())
                ExposedDropdownMenu(expanded = genreExpanded, onDismissRequest = { genreExpanded = false }, modifier = Modifier.background(Color(0xFF1E1E2E))) {
                    GameConstants.GENRES.forEach { g ->
                        DropdownMenuItem(text = { Text(g, color = Color(0xFFE8E8F0)) },
                            onClick = { genre = g; genreError = null; genreExpanded = false })
                    }
                }
            }
            genreError?.let { Text(it, color = Color(0xFFFF5252), fontSize = 12.sp) }

            FormField(value = yearStr, onValueChange = { yearStr = it; yearError = null }, label = stringResource(R.string.field_year), error = yearError, icon = Icons.Default.CalendarToday, keyboardType = KeyboardType.Number, enabled = !isSaving)
            FormField(value = developer, onValueChange = { developer = it; devError = null }, label = stringResource(R.string.field_developer), error = devError, icon = Icons.Default.Business, enabled = !isSaving)

            Column {
                Text(stringResource(R.string.field_rating), color = Color(0xFFB0B0C8), fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(String.format(java.util.Locale.getDefault(), "%.1f", rating), color = Color(0xFFE8E8F0), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(" " + stringResource(R.string.label_current_avg), color = Color(0xFFB0B0C8), fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    if (!validate()) return@Button
                    isSaving = true
                    
                    scope.launch {
                        // Check for duplicate title + platform (excluding current game)
                        val duplicate = viewModel.checkDuplicate(title.trim(), platform)
                        if (duplicate != null && duplicate.id != gameId) {
                            snackbarHostState.showSnackbar(context.getString(R.string.error_duplicate_game))
                            isSaving = false
                            return@launch
                        }

                        val change = PendingChange(
                            changeType             = ChangeType.EDIT_GAME,
                            gameId                 = gameId ?: 0,
                            requestedByDisplayName = UserSession.displayName,
                            originalTitle          = origTitle,
                            originalPlatform       = origPlatform,
                            originalGenre          = origGenre,
                            originalReleaseYear    = origYear,
                            originalRating         = origRating,
                            originalDeveloper      = origDeveloper,
                            originalImageUri       = origImageUri,
                            originalImageBiasX     = origImageBiasX,
                            originalImageBiasY     = origImageBiasY,
                            proposedTitle          = title.trim(),
                            proposedPlatform       = platform,
                            proposedGenre          = genre,
                            proposedReleaseYear    = yearStr.toInt(),
                            proposedRating         = rating,
                            proposedDeveloper      = developer.trim(),
                            proposedImageUri       = imageUri,
                            proposedImageBiasX     = imageBiasX,
                            proposedImageBiasY     = imageBiasY
                        )
                        viewModel.proposePendingChange(change)
                        Toast.makeText(context, context.getString(R.string.msg_edit_proposed), Toast.LENGTH_SHORT).show()
                        onBack()
                    }
                },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C4DFF))
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Send, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.btn_propose), color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
