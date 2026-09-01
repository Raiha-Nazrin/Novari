package com.example.novari.ui.screens.settings.appearnce

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.novari.data.preferences.AppearancePreferences
import com.example.novari.ui.theme.AccentColor
import com.example.novari.ui.theme.AppearanceSettings
import com.example.novari.ui.theme.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AppearanceUiState(
    val saved: AppearanceSettings = AppearanceSettings(),
    val draft: AppearanceSettings = AppearanceSettings(),
    val isSaving: Boolean = false
) {
    val hasUnsavedChanges: Boolean get() = draft != saved
}

sealed interface AppearanceEvent {
    data object Saved : AppearanceEvent
}

@HiltViewModel
class AppearanceViewModel @Inject constructor(
    private val appearancePreferences: AppearancePreferences
) : ViewModel() {

    private val saved = appearancePreferences.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppearanceSettings())
    private val draft = MutableStateFlow<AppearanceSettings?>(null)
    private val isSaving = MutableStateFlow(false)
    private val events = Channel<AppearanceEvent>(Channel.BUFFERED)

    val uiState: StateFlow<AppearanceUiState> = combine(
        saved,
        draft,
        isSaving
    ) { savedValue, draftValue, savingValue ->
        AppearanceUiState(
            saved = savedValue,
            draft = draftValue ?: savedValue,
            isSaving = savingValue
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, AppearanceUiState())

    val eventFlow: Flow<AppearanceEvent> = events.receiveAsFlow()

    fun onThemeSelected(mode: ThemeMode) {
        draft.update { (it ?: saved.value).copy(theme = mode) }
    }

    fun onAccentSelected(accent: AccentColor) {
        draft.update { (it ?: saved.value).copy(accent = accent) }
    }

    fun save() {
        val toSave = draft.value ?: saved.value
        viewModelScope.launch {
            isSaving.value = true
            appearancePreferences.setSettings(toSave)
            isSaving.value = false
            draft.value = null
            events.send(AppearanceEvent.Saved)
        }
    }

    fun discard() {
        draft.value = null
    }
}
