package com.gneticcore.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gneticcore.app.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val scale = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        scale.animateTo(1f, animationSpec = spring(dampingRatio = 0.4f, stiffness = 300f))
        delay(1200)
        onFinished()
    }
    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFF0A0A0F), Color(0xFF1A0A2E), Color(0xFF0A0A0F)))
        ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.SportsEsports, contentDescription = null, tint = Color(0xFF7C4DFF),
                modifier = Modifier.size(100.dp).scale(scale.value))
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.app_name), fontSize = 36.sp, fontWeight = FontWeight.Bold,
                color = Color(0xFF7C4DFF), modifier = Modifier.scale(scale.value))
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.app_tagline), fontSize = 14.sp, color = Color(0xFFB0B0C8))
            Spacer(Modifier.height(40.dp))
            CircularProgressIndicator(color = Color(0xFF7C4DFF), strokeWidth = 2.dp)
        }
    }
}
