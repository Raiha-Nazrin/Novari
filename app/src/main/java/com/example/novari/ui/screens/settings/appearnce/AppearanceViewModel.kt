package com.example.novari.ui.screens.settings.appearnce

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.novari.data.preferences.AppearancePreferences
import com.example.novari.ui.theme.AccentColor
import com.example.novari.ui.theme.AppearanceSettings
import com.example.novari.ui.theme.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppearanceViewModel @Inject constructor(
    private val appearancePreferences: AppearancePreferences
) : ViewModel() {

    val settings: StateFlow<AppearanceSettings> = appearancePreferences.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppearanceSettings())

    fun onThemeSelected(mode: ThemeMode) {
        viewModelScope.launch {
            appearancePreferences.setThemeMode(mode)
        }
    }

    fun onAccentSelected(accent: AccentColor) {
        viewModelScope.launch {
            appearancePreferences.setAccent(accent)
        }
    }
}
