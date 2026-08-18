package com.example.novari.ui.screens.onboarding

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun OnboardingRoute(
    onOnboardingComplete: () -> Unit,
    onSkip: () -> Unit = onOnboardingComplete,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    OnboardingScreen(
        onOnboardingComplete = {
            viewModel.markCompleted()
            onOnboardingComplete()
        },
        onSkip = {
            viewModel.markCompleted()
            onSkip()
        }
    )
}
