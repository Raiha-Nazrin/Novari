package com.example.novari.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.example.novari.ui.motion.LocalReducedMotion
import com.example.novari.ui.motion.rememberReducedMotion

private fun NovariColorScheme.toMaterialScheme(isDark: Boolean): ColorScheme {
    val base = if (isDark) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = teal,
        onPrimary = surface,

        primaryContainer = paleTeal,
        onPrimaryContainer = darkTeal,

        background = background,
        onBackground = navy,

        surface = surface,
        onSurface = navy,

        surfaceVariant = paleTeal,
        onSurfaceVariant = slate,

        outline = border,

        error = error,
        onError = surface
    )
}

@Composable
fun NovariTheme(
    settings: AppearanceSettings = AppearanceSettings(),
    content: @Composable () -> Unit
) {
    val isDark = settings.theme == ThemeMode.DARK
    val colors = if (isDark) darkScheme(settings.accent) else lightScheme(settings.accent)

    CompositionLocalProvider(
        LocalNovariColors provides colors,
        LocalReducedMotion provides rememberReducedMotion()
    ) {
        MaterialTheme(
            colorScheme = colors.toMaterialScheme(isDark),
            typography = NovariTypography,
            content = content
        )
    }
}
