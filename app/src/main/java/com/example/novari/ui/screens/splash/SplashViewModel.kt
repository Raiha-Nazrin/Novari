package com.example.novari.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.novari.data.preferences.OnboardingPreferences
import com.example.novari.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SplashUiState {
    data object Loading : SplashUiState
    data class Ready(val startDestination: String) : SplashUiState
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val onboardingPreferences: OnboardingPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Loading)
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val hasCompletedOnboarding = onboardingPreferences.hasCompletedOnboarding.first()
            val startDestination = if (hasCompletedOnboarding) {
                Screen.Dashboard.route
            } else {
                Screen.Onboarding.route
            }
            _uiState.value = SplashUiState.Ready(startDestination)
        }
    }
}
