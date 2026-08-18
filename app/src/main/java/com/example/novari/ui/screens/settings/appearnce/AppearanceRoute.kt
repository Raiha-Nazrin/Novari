package com.example.novari.ui.screens.settings.appearnce

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun AppearanceRoute(
    onBack: () -> Unit,
    viewModel: AppearanceViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    AppearanceScreen(
        selectedTheme = settings.theme,
        selectedAccent = settings.accent,
        onThemeSelected = viewModel::onThemeSelected,
        onAccentSelected = viewModel::onAccentSelected,
        onBack = onBack
    )
}
