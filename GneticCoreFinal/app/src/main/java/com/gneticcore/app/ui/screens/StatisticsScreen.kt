package com.gneticcore.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(viewModel: GameViewModel, onBack: () -> Unit) {
    val games     by viewModel.games.collectAsStateWithLifecycle()
    val total     = games.size
    val avgRating = if (games.isEmpty()) 0f else games.map { it.rating }.average().toFloat()

    Scaffold(
        containerColor = Color(0xFF0A0A0F),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.section_stats),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE8E8F0)
                    )
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFF0A0A0F))
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Summary cards
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                StatCard(modifier = Modifier.weight(1f), value = "$total",                    label = stringResource(R.string.stat_total_games), color = Color(0xFF7C4DFF), icon = Icons.Default.SportsEsports)
                StatCard(modifier = Modifier.weight(1f), value = "%.1f".format(avgRating),   label = stringResource(R.string.stat_avg_rating),  color = Color(0xFFFFD700), icon = Icons.Default.Star)
            }

            if (total > 0) {
                SectionHeader(stringResource(R.string.stat_by_platform))
                BarChart(
                    data     = GameConstants.PLATFORMS.mapNotNull { p -> val c = games.count { it.platform == p }; if (c > 0) p to c else null },
                    maxValue = total,
                    colorFn  = { GameConstants.getPlatformColor(it) },
                    labelFn  = { "${GameConstants.getPlatformEmoji(it)} $it" }
                )

                SectionHeader(stringResource(R.string.stat_by_genre))
                BarChart(
                    data     = GameConstants.GENRES.mapNotNull { g -> val c = games.count { it.genre == g }; if (c > 0) g to c else null },
                    maxValue = total,
                    colorFn  = { GameConstants.getGenreColor(it) },
                    labelFn  = { it }
                )

                SectionHeader(stringResource(R.string.section_top_rated_titles))
                TopGamesList(games.sortedByDescending { it.rating }.take(5))
            } else {
                Box(Modifier
                    .fillMaxWidth()
                    .padding(40.dp), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.msg_no_games), color = Color(0xFFB0B0C8))
                }
            }
        }
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, value: String, label: String, color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(modifier = modifier, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF13131A))) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(6.dp))
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = color)
            Text(label, fontSize = 10.sp, color = Color(0xFFB0B0C8), maxLines = 1)
        }
    }
}

@Composable
fun BarChart(data: List<Pair<String, Int>>, maxValue: Int, colorFn: (String) -> Color, labelFn: (String) -> String) {
    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF13131A)), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            data.forEach { (key, count) ->
                val fraction = if (maxValue > 0) count.toFloat() / maxValue else 0f
                val barColor = colorFn(key)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(labelFn(key), fontSize = 12.sp, color = Color(0xFFB0B0C8), modifier = Modifier.width(130.dp), maxLines = 1)
                    Spacer(Modifier.width(8.dp))
                    Box(modifier = Modifier
                        .weight(1f)
                        .height(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF1E1E2E))) {
                        Box(modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(fraction)
                            .clip(RoundedCornerShape(4.dp))
                            .background(barColor))
                    }
                    Spacer(Modifier.width(8.dp))
                    Text("$count", fontSize = 12.sp, color = barColor, fontWeight = FontWeight.Bold, modifier = Modifier.width(24.dp))
                }
            }
        }
    }
}

@Composable
fun TopGamesList(games: List<Game>) {
    Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF13131A)), modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            games.forEachIndexed { index, game ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "#${index + 1}", fontSize = 14.sp, fontWeight = FontWeight.Bold,
                        color = when (index) { 0 -> Color(0xFFFFD700); 1 -> Color(0xFFC0C0C0); 2 -> Color(0xFFCD7F32); else -> Color(0xFFB0B0C8) },
                        modifier = Modifier.width(32.dp)
                    )
                    Text(game.title, fontSize = 14.sp, color = Color(0xFFE8E8F0), modifier = Modifier.weight(1f))
                    Row {
                        repeat(5) { i ->
                            Icon(Icons.Default.Star, null,
                                tint = if (i < game.rating) Color(0xFFFFD700) else Color(0xFF3D3D5C),
                                modifier = Modifier.size(12.dp))
                        }
                    }
                }
            }
        }
    }
}
