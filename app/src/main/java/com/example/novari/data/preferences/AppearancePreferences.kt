package com.example.novari.data.preferences

import com.example.novari.ui.theme.AccentColor
import com.example.novari.ui.theme.AppearanceSettings
import com.example.novari.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow

interface AppearancePreferences {
    val settings: Flow<AppearanceSettings>
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setAccent(accent: AccentColor)
    suspend fun setSettings(settings: AppearanceSettings)
}
