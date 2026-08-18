package com.example.novari.ui.theme

import androidx.compose.ui.graphics.Color

enum class ThemeMode {
    LIGHT,
    DARK
}

enum class AccentColor(val seed: Color) {
    TEAL(Color(0xFF087A75)),
    BLUE(Color(0xFF3979B9)),
    PURPLE(Color(0xFF7652A8)),
    PINK(Color(0xFFB53B67)),
    ORANGE(Color(0xFFC88725)),
    GRAY(Color(0xFF6B6F71))
}

data class AppearanceSettings(
    val theme: ThemeMode = ThemeMode.LIGHT,
    val accent: AccentColor = AccentColor.TEAL
)
