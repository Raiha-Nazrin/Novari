package com.example.novari.ui.screens.permissions

import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.novari.permissions.PermissionSettingsIntents

@Composable
fun SetupPermissionRoute(
    onClose: () -> Unit = {},
    onMaybeLater: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: SetupPermissionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = LocalActivity.current

    val rationale = remember(activity) {
        RationaleProvider { permission ->
            activity?.let { ActivityCompat.shouldShowRequestPermissionRationale(it, permission) } ?: false
        }
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.refresh(rationale)
    }

    LifecycleResumeEffect(rationale) {
        viewModel.refresh(rationale)
        onPauseOrDispose { }
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is PermissionEffect.RequestRuntimePermissions ->
                    launcher.launch(effect.permissions.toTypedArray())

                is PermissionEffect.OpenSettings ->
                    PermissionSettingsIntents.open(context, effect.type)
            }
        }
    }

    SetupPermissionScreen(
        uiState = uiState,
        onPermissionAction = { type -> viewModel.onCardAction(type, rationale) },
        onClose = onClose,
        onMaybeLater = onMaybeLater,
        modifier = modifier
    )
}
