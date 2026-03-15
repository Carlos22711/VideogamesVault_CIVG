package com.gneticcore.app.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.gneticcore.app.R
import com.gneticcore.app.data.entity.Game
import com.gneticcore.app.ui.viewmodel.GameViewModel
import com.gneticcore.app.utils.GameConstants
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameFormScreen(
    viewModel: GameViewModel,
    gameId: Int?,
    isAdmin: Boolean,
    onBack: () -> Unit
) {
    val scope    = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val context  = LocalContext.current
    var existingGame by remember { mutableStateOf<Game?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(gameId) {
        if (gameId != null && gameId > 0) existingGame = viewModel.getGameById(gameId)
    }

    var title      by remember { mutableStateOf("") }
    var platform   by remember { mutableStateOf("") }
    var genre      by remember { mutableStateOf("") }
    var yearStr    by remember { mutableStateOf("") }
    var developer  by remember { mutableStateOf("") }
    var rating     by remember { mutableStateOf(0f) }
    var imageUri   by remember { mutableStateOf("") }
    var imageBiasX by remember { mutableStateOf(0.5f) }
    var imageBiasY by remember { mutableStateOf(0.5f) }

    LaunchedEffect(existingGame) {
        existingGame?.let { g ->
            title = g.title; platform = g.platform; genre = g.genre
            yearStr = g.releaseYear.toString(); developer = g.developer
            rating = g.rating; imageUri = g.imageUri
            imageBiasX = g.imageBiasX; imageBiasY = g.imageBiasY
        }
    }

    // Calcular URI por defecto según plataforma
    val defaultPlatformUri = remember(platform) {
        val logoName = GameConstants.getPlatformLogoName(platform)
        if (logoName.isNotBlank()) "android.resource://${context.packageName}/drawable/$logoName" else ""
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(
                it, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            imageUri = it.toString()
        }
    }

    var titleError    by remember { mutableStateOf<String?>(null) }
    var platformError by remember { mutableStateOf<String?>(null) }
    var genreError    by remember { mutableStateOf<String?>(null) }
    var yearError     by remember { mutableStateOf<String?>(null) }
    var devError      by remember { mutableStateOf<String?>(null) }
    var ratingError   by remember { mutableStateOf<String?>(null) }
    var platformExpanded by remember { mutableStateOf(false) }
    var genreExpanded    by remember { mutableStateOf(false) }

    val isEditing = existingGame != null

    fun validate(): Boolean {
        var ok = true
        titleError    = if (title.isBlank()) { ok=false; context.getString(R.string.error_required) } else if (title.length<2) { ok=false; context.getString(R.string.error_min_chars) } else null
        platformError = if (platform.isBlank()) { ok=false; context.getString(R.string.hint_select_platform) } else null
        genreError    = if (genre.isBlank()) { ok=false; context.getString(R.string.hint_select_genre) } else null
        yearError = when {
            yearStr.isBlank()             -> { ok=false; context.getString(R.string.error_required) }
            yearStr.toIntOrNull() == null -> { ok=false; context.getString(R.string.error_year_invalid) }
            yearStr.toInt() !in GameConstants.MIN_YEAR..GameConstants.MAX_YEAR -> { ok=false; context.getString(R.string.error_year_range, GameConstants.MIN_YEAR, GameConstants.MAX_YEAR) }
            else -> null
        }
        devError    = if (developer.isBlank()) { ok=false; context.getString(R.string.error_required) } else if (developer.length<2) { ok=false; context.getString(R.string.error_min_chars) } else null
        ratingError = if (rating < 1f) { ok=false; context.getString(R.string.error_rating_required) } else null
        return ok
    }

    Scaffold(
        containerColor = Color(0xFF0A0A0F),
        snackbarHost   = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEditing) stringResource(R.string.section_edit_game) else stringResource(R.string.section_add_new_game),
                        fontWeight = FontWeight.Bold, color = Color(0xFFE8E8F0)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack, enabled = !isSaving) {
                        Icon(Icons.Default.ArrowBack, null, tint = if (isSaving) Color.Gray else Color(0xFFB0B0C8))
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

            // ── Image picker & Focus Adjustment ──
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.label_game_cover), color = Color(0xFFB0B0C8), fontSize = 14.sp)
                Text(stringResource(R.string.label_optional), color = Color(0xFF7C4DFF), fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E1E2E))
                        .border(1.dp, Color(0xFF3D3D5C), RoundedCornerShape(12.dp))
                        .pointerInput(imageUri, defaultPlatformUri) {
                            if (imageUri.isNotBlank() || defaultPlatformUri.isNotBlank()) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    val sensitivity = 1.5f
                                    val newX = (imageBiasX - (dragAmount.x / size.width) * sensitivity).coerceIn(0f, 1f)
                                    val newY = (imageBiasY - (dragAmount.y / size.height) * sensitivity).coerceIn(0f, 1f)
                                    imageBiasX = newX
                                    imageBiasY = newY
                                }
                            }
                        }
                        .clickable(enabled = !isSaving) { galleryLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    val uriToDisplay = imageUri.ifBlank { defaultPlatformUri }
                    
                    if (uriToDisplay.isNotBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context).data(Uri.parse(uriToDisplay)).crossfade(true).build(),
                            contentDescription = stringResource(R.string.content_desc_cover),
                            contentScale = ContentScale.Crop,
                            alignment = BiasAlignment((imageBiasX * 2f) - 1f, (imageBiasY * 2f) - 1f),
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier.fillMaxSize().background(Color(0x11000000)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.OpenWith, null, tint = Color.White.copy(alpha = 0.4f), modifier = Modifier.size(48.dp))
                        }
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.BottomEnd
                        ) {
                            Icon(Icons.Default.Edit, "Cambiar", tint = Color.White, modifier = Modifier.padding(12.dp).size(24.dp))
                        }
                    } else {
                        // Vista previa por defecto total (sin plataforma seleccionada aún)
                        Box(
                            modifier = Modifier.fillMaxSize().background(
                                Brush.verticalGradient(listOf(Color(0xFF7C4DFF).copy(alpha = 0.3f), Color(0xFF1E1E2E)))
                            ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🎮", fontSize = 48.sp)
                                Spacer(Modifier.height(12.dp))
                                Icon(Icons.Default.AddPhotoAlternate, "Agregar imagen", tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(32.dp))
                                Text(stringResource(R.string.label_tap_to_upload), color = Color(0xFFB0B0C8), fontSize = 12.sp)
                            }
                        }
                    }
                }
                
                if (imageUri.isNotBlank() || defaultPlatformUri.isNotBlank()) {
                    Text(
                        stringResource(R.string.label_adjust_focus_hint),
                        color = Color(0xFF7C4DFF),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
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
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (1..5).forEach { star ->
                        IconButton(onClick = { rating = star.toFloat(); ratingError = null }, modifier = Modifier.size(40.dp), enabled = !isSaving) {
                            Icon(Icons.Default.Star, null, tint = if (star <= rating) Color(0xFFFFD700) else Color(0xFF3D3D5C), modifier = Modifier.size(32.dp))
                        }
                    }
                    Text(if (rating > 0) "${rating.toInt()}/5" else "", color = Color(0xFFB0B0C8), modifier = Modifier.align(Alignment.CenterVertically))
                }
                ratingError?.let { Text(it, color = Color(0xFFFF5252), fontSize = 12.sp) }
            }

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    if (!validate()) return@Button
                    isSaving = true
                    
                    scope.launch {
                        val duplicate = viewModel.checkDuplicate(title.trim(), platform)
                        if (duplicate != null && (!isEditing || duplicate.id != existingGame?.id)) {
                            snackbar.showSnackbar(context.getString(R.string.error_duplicate_game))
                            isSaving = false
                            return@launch
                        }

                        // Si no hay imagen, usamos la de la plataforma por defecto
                        val finalImageUri = imageUri.ifBlank { defaultPlatformUri }

                        val year = yearStr.toInt()
                        val gameData = Game(
                            title=title, platform=platform, genre=genre, releaseYear=year,
                            rating=rating, developer=developer,
                            imageUri=finalImageUri, imageBiasX=imageBiasX, imageBiasY=imageBiasY
                        )
                        
                        val messageRes = if (isAdmin) {
                            if (isEditing) {
                                viewModel.updateGame(gameData.copy(id = existingGame!!.id))
                                R.string.msg_game_updated
                            } else {
                                viewModel.insertGame(gameData)
                                R.string.msg_game_added
                            }
                        } else {
                            viewModel.proposeNewGame(gameData)
                            R.string.msg_new_game_proposed
                        }
                        
                        Toast.makeText(context, context.getString(messageRes), Toast.LENGTH_SHORT).show()
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
                    Icon(Icons.Default.Save, null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text(if (isAdmin) stringResource(R.string.btn_save) else stringResource(R.string.btn_propose), color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String?,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange,
        label = { Text(label, color = Color(0xFFB0B0C8)) },
        leadingIcon = { Icon(icon, null, tint = Color(0xFF7C4DFF)) },
        isError = error != null,
        enabled = enabled,
        supportingText = error?.let { { Text(it, color = Color(0xFFFF5252)) } },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = formFieldColors()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun formFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor   = Color(0xFF7C4DFF),
    unfocusedBorderColor = Color(0xFF3D3D5C),
    focusedTextColor     = Color(0xFFE8E8F0),
    unfocusedTextColor   = Color(0xFFE8E8F0),
    cursorColor          = Color(0xFF7C4DFF),
    errorBorderColor     = Color(0xFFFF5252),
    focusedLabelColor    = Color(0xFF7C4DFF),
    disabledBorderColor  = Color(0xFF3D3D5C),
    disabledTextColor    = Color(0xFFB0B0C8),
    disabledLabelColor   = Color(0xFFB0B0C8)
)
