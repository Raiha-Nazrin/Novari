package com.example.novari.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/**
 * Theme-and-accent-resolved palette. Values that are fixed per theme (background, text,
 * borders) are literal; values derived from the user's chosen accent (teal/darkTeal/paleTeal,
 * and the tokens that follow them) are computed from [AccentColor.seed].
 */
data class NovariColorScheme(
    val background: Color,
    val surface: Color,
    val surfaceHigh: Color,

    val teal: Color,
    val darkTeal: Color,
    val paleTeal: Color,
    val mint: Color,

    val navy: Color,
    val slate: Color,
    val muted: Color,

    val border: Color,
    val divider: Color,

    val success: Color,
    val error: Color,
    val errorDark: Color,
    val errorBackground: Color,

    val chartLine: Color,
    val chartFill: Color,
    val chartGrid: Color
)

val LocalNovariColors = staticCompositionLocalOf { lightScheme(AccentColor.TEAL) }

private fun Color.lighten(fraction: Float): Color = lerp(this, Color.White, fraction)
private fun Color.darken(fraction: Float): Color = lerp(this, Color.Black, fraction)
private fun blend(base: Color, tint: Color, fraction: Float): Color = lerp(base, tint, fraction)

fun lightScheme(accent: AccentColor): NovariColorScheme {
    val background = Color(0xFFF8F7F3)
    val surface = Color(0xFFFFFEFC)
    val seed = accent.seed

    return NovariColorScheme(
        background = background,
        surface = surface,
        surfaceHigh = Color(0xFFF1F0EC),

        teal = seed,
        darkTeal = seed.darken(0.2f),
        paleTeal = blend(surface, seed, 0.09f),
        mint = blend(surface, seed, 0.14f),

        navy = Color(0xFF102A43),
        slate = Color(0xFF667085),
        muted = Color(0xFF98A2B3),

        border = Color(0xFFE2E1DD),
        divider = Color(0xFFE8E7E3),

        success = seed,
        error = Color(0xFFE5484D),
        errorDark = Color(0xFFD92D35),
        errorBackground = Color(0xFFFDECEC),

        chartLine = seed,
        chartFill = blend(surface, seed, 0.14f),
        chartGrid = Color(0xFFE5E7E5)
    )
}

fun darkScheme(accent: AccentColor): NovariColorScheme {
    val background = Color(0xFF141614)
    val surface = Color(0xFF1D211F)
    val seed = accent.seed
    val error = Color(0xFFFF6B6F)

    return NovariColorScheme(
        background = background,
        surface = surface,
        surfaceHigh = Color(0xFF262B29),

        teal = seed.lighten(0.18f),
        darkTeal = seed.lighten(0.32f),
        paleTeal = blend(surface, seed, 0.24f),
        mint = blend(surface, seed, 0.30f),

        navy = Color(0xFFE9EDEA),
        slate = Color(0xFFA2ADA8),
        muted = Color(0xFF6F7A76),

        border = Color(0xFF2C3230),
        divider = Color(0xFF232826),

        success = seed.lighten(0.18f),
        error = error,
        errorDark = error.lighten(0.12f),
        errorBackground = blend(surface, error, 0.14f),

        chartLine = seed.lighten(0.18f),
        chartFill = blend(surface, seed, 0.30f),
        chartGrid = Color(0xFF2A302E)
    )
}
