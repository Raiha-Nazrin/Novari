package com.example.novari.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

object NovariColors {

    // Background
    val Background: Color
        @Composable @ReadOnlyComposable get() = LocalNovariColors.current.background
    val Surface: Color
        @Composable @ReadOnlyComposable get() = LocalNovariColors.current.surface
    val SurfaceHigh: Color
        @Composable @ReadOnlyComposable get() = LocalNovariColors.current.surfaceHigh

    // Brand
    val Teal: Color
        @Composable @ReadOnlyComposable get() = LocalNovariColors.current.teal
    val DarkTeal: Color
        @Composable @ReadOnlyComposable get() = LocalNovariColors.current.darkTeal
    val PaleTeal: Color
        @Composable @ReadOnlyComposable get() = LocalNovariColors.current.paleTeal
    val Mint: Color
        @Composable @ReadOnlyComposable get() = LocalNovariColors.current.mint

    // Text
    val Navy: Color
        @Composable @ReadOnlyComposable get() = LocalNovariColors.current.navy
    val Slate: Color
        @Composable @ReadOnlyComposable get() = LocalNovariColors.current.slate
    val Muted: Color
        @Composable @ReadOnlyComposable get() = LocalNovariColors.current.muted

    // Borders
    val Border: Color
        @Composable @ReadOnlyComposable get() = LocalNovariColors.current.border
    val Divider: Color
        @Composable @ReadOnlyComposable get() = LocalNovariColors.current.divider

    // Semantic
    val Success: Color
        @Composable @ReadOnlyComposable get() = LocalNovariColors.current.success
    val Error: Color
        @Composable @ReadOnlyComposable get() = LocalNovariColors.current.error
    val ErrorDark: Color
        @Composable @ReadOnlyComposable get() = LocalNovariColors.current.errorDark
    val ErrorBackground: Color
        @Composable @ReadOnlyComposable get() = LocalNovariColors.current.errorBackground

    // Categories — identity, not decoration: fixed across themes.
    val Food = Color(0xFF087A75)
    val FoodBackground = Color(0xFFE8F3F0)

    val Shopping = Color(0xFF4A9B83)
    val ShoppingBackground = Color(0xFFE7F1EC)

    val Transport = Color(0xFF587A87)
    val TransportBackground = Color(0xFFE8EFF1)

    val Others = Color(0xFF7A7F86)
    val OthersBackground = Color(0xFFEFF0EF)

    // Chart
    val ChartLine: Color
        @Composable @ReadOnlyComposable get() = LocalNovariColors.current.chartLine
    val ChartFill: Color
        @Composable @ReadOnlyComposable get() = LocalNovariColors.current.chartFill
    val ChartGrid: Color
        @Composable @ReadOnlyComposable get() = LocalNovariColors.current.chartGrid
}
