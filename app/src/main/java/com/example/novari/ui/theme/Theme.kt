package com.example.novari.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val NovariLightColors = lightColorScheme(

    primary = NovariColors.Teal,
    onPrimary = NovariColors.Surface,

    primaryContainer = NovariColors.PaleTeal,
    onPrimaryContainer = NovariColors.DarkTeal,

    background = NovariColors.Background,
    onBackground = NovariColors.Navy,

    surface = NovariColors.Surface,
    onSurface = NovariColors.Navy,

    surfaceVariant = NovariColors.PaleTeal,
    onSurfaceVariant = NovariColors.Slate,

    outline = NovariColors.Border,

    error = NovariColors.Error,
    onError = NovariColors.Surface
)

@Composable
fun NovariTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = NovariLightColors,
        typography = NovariTypography,
        content = content
    )
}