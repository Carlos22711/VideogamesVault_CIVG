package com.gneticcore.app.ui.screens

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.gneticcore.app.R
import com.gneticcore.app.data.entity.Game
import com.gneticcore.app.ui.viewmodel.GameViewModel
import com.gneticcore.app.utils.GameConstants
import com.gneticcore.app.utils.UserSession

enum class UserTab { ALL, BY_PLATFORM, TOP_RATED, RECENT }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserHomeScreen(
    viewModel: GameViewModel,
    onGameClick: (Int) -> Unit,
    onAddGame: () -> Unit,
    onProposeEdit: (Int) -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(UserTab.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }

    val allGames    by viewModel.games.collectAsStateWithLifecycle()
    val topRated    by viewModel.topRatedGames.collectAsStateWithLifecycle()
    val recent      by viewModel.recentGames.collectAsStateWithLifecycle()

    val baseList: List<Game> = when (selectedTab) {
        UserTab.ALL         -> allGames
        UserTab.TOP_RATED   -> topRated
        UserTab.RECENT      -> recent
        UserTab.BY_PLATFORM -> allGames
    }

    val displayList = remember(baseList, searchQuery) {
        if (searchQuery.isBlank()) baseList
        else baseList.filter { it.title.contains(searchQuery, ignoreCase = true) }
    }

    Scaffold(
        containerColor = Color(0xFF0A0A0F),
        topBar = {
            TopAppBar(
                title = { 
                    if (isSearchExpanded) {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text(stringResource(R.string.hint_search), color = Color(0xFFB0B0C8)) },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color(0xFF7C4DFF)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(stringResource(R.string.app_title_with_emoji), fontWeight = FontWeight.Bold, color = Color(0xFF7C4DFF)) 
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF13131A)),
                actions = {
                    IconButton(onClick = { 
                        isSearchExpanded = !isSearchExpanded 
                        if (!isSearchExpanded) searchQuery = ""
                    }) {
                        Icon(if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search, contentDescription = stringResource(R.string.hint_search), tint = Color(0xFFB0B0C8))
                    }
                    Surface(shape = MaterialTheme.shapes.small, color = Color(0xFF1E1E2E), modifier = Modifier.padding(end = 4.dp)) {
                        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, null, tint = Color(0xFF00E5FF), modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(UserSession.displayName, color = Color(0xFF00E5FF), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = stringResource(R.string.btn_logout), tint = Color(0xFFB0B0C8))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddGame, containerColor = Color(0xFF7C4DFF)) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.btn_add_game), tint = Color.White)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFF0A0A0F))) {
            ScrollableTabRow(selectedTabIndex = selectedTab.ordinal, containerColor = Color(0xFF13131A), contentColor = Color(0xFF7C4DFF), edgePadding = 8.dp) {
                UserTab.entries.forEach { tab ->
                    Tab(selected = selectedTab == tab, onClick = { selectedTab = tab }, text = {
                        Text(fontSize = 12.sp, text = when (tab) {
                            UserTab.ALL         -> stringResource(R.string.nav_all_games)
                            UserTab.BY_PLATFORM -> stringResource(R.string.nav_by_platform)
                            UserTab.TOP_RATED   -> stringResource(R.string.nav_top_rated)
                            UserTab.RECENT      -> stringResource(R.string.nav_recent)
                        })
                    })
                }
            }

            if (selectedTab == UserTab.BY_PLATFORM) {
                ByPlatformView(games = displayList, onGameClick = onGameClick, onProposeEdit = onProposeEdit)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    if (displayList.isEmpty()) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (searchQuery.isBlank()) stringResource(R.string.msg_no_games) 
                                           else stringResource(R.string.msg_no_results_for, searchQuery), 
                                    color = Color(0xFFB0B0C8)
                                )
                            }
                        }
                    } else {
                        items(displayList, key = { it.id }) { game ->
                            GameCard(game = game, onClick = { onGameClick(game.id) }, onProposeEdit = { onProposeEdit(game.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ByPlatformView(games: List<Game>, onGameClick: (Int) -> Unit, onProposeEdit: (Int) -> Unit) {
    val grouped  = games.groupBy { it.platform }
    val expanded = remember { mutableStateMapOf<String, Boolean>() }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        GameConstants.PLATFORMS.forEach { platform ->
            val platformGames = grouped[platform] ?: emptyList()
            if (platformGames.isNotEmpty()) {
                val isExpanded = expanded[platform] ?: false
                item {
                    PlatformHeader(
                        platform   = platform,
                        gameCount  = platformGames.size,
                        isExpanded = isExpanded,
                        onClick    = { expanded[platform] = !isExpanded }
                    )
                }
                if (isExpanded) {
                    items(platformGames, key = { it.id }) { game ->
                        GameCard(game = game, onClick = { onGameClick(game.id) }, onProposeEdit = { onProposeEdit(game.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun PlatformHeader(platform: String, gameCount: Int, isExpanded: Boolean, onClick: () -> Unit) {
    val context      = LocalContext.current
    val logoName     = GameConstants.getPlatformLogoName(platform)
    val platformColor = GameConstants.getPlatformColor(platform)
    val logoResId    = remember(logoName) {
        if (logoName.isNotBlank())
            context.resources.getIdentifier(logoName, "drawable", context.packageName)
        else 0
    }

    Card(
        onClick = onClick,
        colors  = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2E)),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(platformColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                if (logoResId != 0) {
                    androidx.compose.foundation.Image(
                        painter = painterResource(id = logoResId),
                        contentDescription = platform,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(36.dp)
                    )
                } else {
                    Text(GameConstants.getPlatformEmoji(platform), fontSize = 24.sp)
                }
            }
            Spacer(Modifier.width(12.dp))
            val gameCountText = context.resources.getQuantityString(R.plurals.game_count, gameCount, gameCount)
            Column(modifier = Modifier.weight(1f)) {
                Text(platform, color = platformColor, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(gameCountText, color = Color(0xFFB0B0C8), fontSize = 12.sp)
            }
            Icon(
                if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                null, tint = Color(0xFFB0B0C8)
            )
        }
    }
}
