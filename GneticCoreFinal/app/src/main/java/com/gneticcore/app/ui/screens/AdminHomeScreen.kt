package com.gneticcore.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gneticcore.app.R
import com.gneticcore.app.data.entity.Game
import com.gneticcore.app.ui.viewmodel.GameViewModel
import com.gneticcore.app.utils.GameConstants
import com.gneticcore.app.utils.UserSession

enum class AdminTab { ALL, BY_PLATFORM, TOP_RATED, RECENT }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(
    viewModel: GameViewModel,
    onGameClick: (Int) -> Unit,
    onEditGame: (Int) -> Unit,
    onPending: () -> Unit,
    onStatistics: () -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab  by remember { mutableStateOf<AdminTab>(AdminTab.ALL) }
    var searchQuery  by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }

    val allGames     by viewModel.games.collectAsStateWithLifecycle()
    val topRated     by viewModel.topRatedGames.collectAsStateWithLifecycle()
    val recent       by viewModel.recentGames.collectAsStateWithLifecycle()
    val pendingCount by viewModel.pendingCount.collectAsStateWithLifecycle()
    var gameToDelete by remember { mutableStateOf<Game?>(null) }

    val baseList: List<Game> = when (selectedTab) {
        AdminTab.ALL         -> allGames
        AdminTab.TOP_RATED   -> topRated
        AdminTab.RECENT      -> recent
        AdminTab.BY_PLATFORM -> allGames
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
                    BadgedBox(badge = { if (pendingCount > 0) Badge { Text("$pendingCount") } }) {
                        IconButton(onClick = onPending) {
                            Icon(Icons.Default.Notifications, contentDescription = stringResource(R.string.nav_pending), tint = Color(0xFFFF6D00))
                        }
                    }
                    IconButton(onClick = onStatistics) {
                        Icon(Icons.Default.BarChart, contentDescription = stringResource(R.string.nav_stats), tint = Color(0xFF00E5FF))
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = stringResource(R.string.btn_logout), tint = Color(0xFFB0B0C8))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onEditGame(-1) }, containerColor = Color(0xFF7C4DFF)) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.btn_add_game), tint = Color.White)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFF0A0A0F))) {
            // Admin banner
            Row(
                modifier = Modifier.fillMaxWidth().background(Color(0xFF4527A0).copy(alpha = 0.3f)).padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.AdminPanelSettings, null, tint = Color(0xFF7C4DFF), modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(UserSession.displayName, color = Color(0xFF7C4DFF), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(" · ${stringResource(R.string.role_admin)}", color = Color(0xFFB0B0C8), fontSize = 12.sp)
                if (pendingCount > 0) {
                    Spacer(Modifier.width(12.dp))
                    Text("$pendingCount ${stringResource(R.string.label_pending_count)}", color = Color(0xFFFF6D00), fontSize = 12.sp)
                }
            }

            ScrollableTabRow(selectedTabIndex = selectedTab.ordinal, containerColor = Color(0xFF13131A), contentColor = Color(0xFF7C4DFF), edgePadding = 8.dp) {
                AdminTab.entries.forEach { tab ->
                    Tab(selected = selectedTab == tab, onClick = { selectedTab = tab }, text = {
                        Text(fontSize = 12.sp, text = when (tab) {
                            AdminTab.ALL         -> stringResource(R.string.nav_all_games)
                            AdminTab.BY_PLATFORM -> stringResource(R.string.nav_by_platform)
                            AdminTab.TOP_RATED   -> stringResource(R.string.nav_top_rated)
                            AdminTab.RECENT      -> stringResource(R.string.nav_recent)
                        })
                    })
                }
            }

            if (selectedTab == AdminTab.BY_PLATFORM) {
                AdminByPlatformView(games = displayList, onGameClick = onGameClick, onEdit = onEditGame, onDelete = { gameToDelete = it })
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    if (displayList.isEmpty()) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                                Text(searchQuery.let { if(it.isBlank()) stringResource(R.string.msg_no_games) else stringResource(R.string.msg_no_results_for, it) }, color = Color(0xFFB0B0C8))
                            }
                        }
                    } else {
                        items(displayList, key = { it.id }) { game ->
                            GameCard(game = game, onClick = { onGameClick(game.id) }, onEdit = { onEditGame(game.id) }, onDelete = { gameToDelete = game })
                        }
                    }
                }
            }
        }
    }

    gameToDelete?.let { game ->
        AlertDialog(
            onDismissRequest = { gameToDelete = null },
            containerColor = Color(0xFF1E1E2E),
            title = { Text(stringResource(R.string.btn_delete), color = Color(0xFFE8E8F0), fontWeight = FontWeight.Bold) },
            text  = { Text(stringResource(R.string.msg_delete_confirm, game.title), color = Color(0xFFB0B0C8)) },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteGame(game); gameToDelete = null }) {
                    Text(stringResource(R.string.btn_delete), color = Color(0xFFFF5252))
                }
            },
            dismissButton = {
                TextButton(onClick = { gameToDelete = null }) {
                    Text(stringResource(R.string.btn_cancel), color = Color(0xFFB0B0C8))
                }
            }
        )
    }
}

@Composable
fun AdminByPlatformView(games: List<Game>, onGameClick: (Int) -> Unit, onEdit: (Int) -> Unit, onDelete: (Game) -> Unit) {
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
                    PlatformHeader(platform = platform, gameCount = platformGames.size, isExpanded = isExpanded, onClick = { expanded[platform] = !isExpanded })
                }
                if (isExpanded) {
                    items(platformGames, key = { it.id }) { game ->
                        GameCard(game = game, onClick = { onGameClick(game.id) }, onEdit = { onEdit(game.id) }, onDelete = { onDelete(game) })
                    }
                }
            }
        }
    }
}
