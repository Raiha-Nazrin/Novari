package com.example.novari.ui.screens.splash

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun SplashRoute(
    onNavigateToStart: (startDestination: String) -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isAnimationFinished by remember { mutableStateOf(false) }

    LaunchedEffect(uiState, isAnimationFinished) {
        val state = uiState
        if (isAnimationFinished && state is SplashUiState.Ready) {
            onNavigateToStart(state.startDestination)
        }
    }

    SplashScreen(onSplashFinished = { isAnimationFinished = true })
}
