package com.example.novari.ui.screens.settings.appearnce

import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.novari.R
import com.example.novari.ui.theme.NovariColors
import com.example.novari.ui.theme.NovariShape

@Composable
fun AppearanceRoute(
    onBack: () -> Unit,
    viewModel: AppearanceViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showUnsavedDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                AppearanceEvent.Saved -> onBack()
            }
        }
    }

    val requestBack: () -> Unit = {
        if (uiState.hasUnsavedChanges) {
            showUnsavedDialog = true
        } else {
            onBack()
        }
    }

    BackHandler(enabled = uiState.hasUnsavedChanges) {
        showUnsavedDialog = true
    }

    AppearanceScreen(
        draft = uiState.draft,
        hasUnsavedChanges = uiState.hasUnsavedChanges,
        isSaving = uiState.isSaving,
        onThemeSelected = viewModel::onThemeSelected,
        onAccentSelected = viewModel::onAccentSelected,
        onSave = viewModel::save,
        onBack = requestBack
    )

    if (showUnsavedDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedDialog = false },
            containerColor = NovariColors.Surface,
            shape = NovariShape.card,
            title = {
                Text(
                    text = stringResource(R.string.unsaved_changes_title),
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.unsaved_changes_message),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showUnsavedDialog = false
                    viewModel.save()
                }) {
                    Text(text = stringResource(R.string.save_changes))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showUnsavedDialog = false
                    viewModel.discard()
                    onBack()
                }) {
                    Text(text = stringResource(R.string.discard_changes))
                }
                TextButton(onClick = { showUnsavedDialog = false }) {
                    Text(text = stringResource(R.string.cancel))
                }
            }
        )
    }
}
