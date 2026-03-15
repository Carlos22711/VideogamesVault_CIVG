package com.gneticcore.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkScheme = darkColorScheme(
    primary             = Color(0xFF7C4DFF),
    onPrimary           = Color(0xFFFFFFFF),
    primaryContainer    = Color(0xFF4527A0),
    secondary           = Color(0xFF00E5FF),
    onSecondary         = Color(0xFF000000),
    secondaryContainer  = Color(0xFF006064),
    tertiary            = Color(0xFFFF6D00),
    background          = Color(0xFF0A0A0F),
    onBackground        = Color(0xFFE8E8F0),
    surface             = Color(0xFF13131A),
    onSurface           = Color(0xFFE8E8F0),
    surfaceVariant      = Color(0xFF1E1E2E),
    onSurfaceVariant    = Color(0xFFB0B0C8),
    error               = Color(0xFFFF5252),
)

@Composable
fun GneticCoreTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = DarkScheme, content = content)
}
