package com.example.novari

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.novari.data.preferences.AppearancePreferences
import com.example.novari.ui.theme.AppearanceSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    appearancePreferences: AppearancePreferences
) : ViewModel() {

    private val _settings = MutableStateFlow<AppearanceSettings?>(null)
    val settings: StateFlow<AppearanceSettings?> = _settings.asStateFlow()

    init {
        viewModelScope.launch {
            appearancePreferences.settings.collect { _settings.value = it }
        }
    }
}
