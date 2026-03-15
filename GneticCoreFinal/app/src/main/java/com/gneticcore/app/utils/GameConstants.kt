package com.gneticcore.app.utils

import androidx.compose.ui.graphics.Color
import java.util.Calendar

object GameConstants {

    val PLATFORMS = listOf(
        "PC",
        "PlayStation 5",
        "PlayStation 4",
        "Xbox Series X/S",
        "Xbox One",
        "Nintendo Switch",
        "iOS",
        "Android"
    )

    val GENRES = listOf(
        "Acción", "Aventura", "RPG", "Estrategia", "Deportes",
        "Carreras", "Terror", "Simulación", "Plataformas", "Puzzle"
    )

    const val MIN_YEAR = 1972
    // Año máximo dinámico basado en el año actual del sistema
    val MAX_YEAR = Calendar.getInstance().get(Calendar.YEAR)

    // Nombre del recurso drawable para cada logo de plataforma
    fun getPlatformLogoName(platform: String): String = when (platform) {
        "PC"              -> "logo_pc"
        "PlayStation 5"   -> "logo_ps5"
        "PlayStation 4"   -> "logo_ps4"
        "Xbox Series X/S" -> "logo_xbox_series"
        "Xbox One"        -> "logo_xbox_one"
        "Nintendo Switch" -> "logo_switch"
        "iOS"             -> "logo_ios"
        "Android"         -> "logo_android"
        else              -> ""
    }

    fun getPlatformColor(platform: String): Color = when (platform) {
        "PC"              -> Color(0xFF1565C0)
        "PlayStation 5",
        "PlayStation 4"   -> Color(0xFF0070CC)
        "Xbox Series X/S",
        "Xbox One"        -> Color(0xFF107C10)
        "Nintendo Switch" -> Color(0xFFE4000F)
        "iOS"             -> Color(0xFF888888)
        "Android"         -> Color(0xFF3DDC84)
        else              -> Color(0xFF546E7A)
    }

    fun getGenreColor(genre: String): Color = when (genre) {
        "Acción"      -> Color(0xFFD32F2F)
        "Aventura"    -> Color(0xFF1976D2)
        "RPG"         -> Color(0xFF7B1FA2)
        "Estrategia"  -> Color(0xFF388E3C)
        "Deportes"    -> Color(0xFFF57C00)
        "Carreras"    -> Color(0xFFE64A19)
        "Terror"      -> Color(0xFF37474F)
        "Simulación"  -> Color(0xFF00796B)
        "Plataformas" -> Color(0xFFC62828)
        "Puzzle"      -> Color(0xFF0288D1)
        else          -> Color(0xFF546E7A)
    }

    fun getPlatformEmoji(platform: String): String = when (platform) {
        "PC"              -> "🖥️"
        "PlayStation 5",
        "PlayStation 4"   -> "🎮"
        "Xbox Series X/S",
        "Xbox One"        -> "🟩"
        "Nintendo Switch" -> "🔴"
        "iOS"             -> "📱"
        "Android"         -> "🤖"
        else              -> "🎮"
    }
}
