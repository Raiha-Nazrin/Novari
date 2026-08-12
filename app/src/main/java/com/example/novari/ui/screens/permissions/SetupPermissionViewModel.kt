package com.example.novari.ui.screens.permissions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.novari.permissions.PermissionChecker
import com.example.novari.permissions.PermissionRequestHistoryStore
import com.example.novari.permissions.PermissionStatus
import com.example.novari.permissions.PermissionStatusResolver
import com.example.novari.permissions.PermissionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Reads rationale per-permission without the ViewModel ever holding an Activity reference. */
fun interface RationaleProvider {
    fun shouldShowRationale(permission: String): Boolean
}

@HiltViewModel
class SetupPermissionViewModel @Inject constructor(
    private val permissionChecker: PermissionChecker,
    private val historyStore: PermissionRequestHistoryStore,
    private val resolver: PermissionStatusResolver
) : ViewModel() {

    private val _uiState = MutableStateFlow(SetupPermissionUiState())
    val uiState: StateFlow<SetupPermissionUiState> = _uiState.asStateFlow()

    private val _effects = Channel<PermissionEffect>(Channel.BUFFERED, BufferOverflow.SUSPEND)
    val effects: Flow<PermissionEffect> = _effects.receiveAsFlow()

    fun refresh(rationale: RationaleProvider) {
        viewModelScope.launch {
            val sms = statusFor(PermissionType.SMS, rationale)
            val notifications = statusFor(PermissionType.NOTIFICATIONS, rationale)
            _uiState.value = _uiState.value.copy(
                sms = sms,
                notifications = notifications,
                requestInFlight = null
            )
        }
    }

    fun onCardAction(type: PermissionType, rationale: RationaleProvider) {
        val state = _uiState.value
        val currentStatus = if (type == PermissionType.SMS) state.sms else state.notifications

        if (currentStatus == PermissionStatus.GRANTED) return
        if (state.requestInFlight != null) return

        val runtimePermissions = permissionChecker.runtimePermissions(type)
        val canRequestDialog = runtimePermissions.isNotEmpty() &&
            (currentStatus == PermissionStatus.NOT_REQUESTED || currentStatus == PermissionStatus.DENIED)

        if (canRequestDialog) {
            viewModelScope.launch {
                historyStore.markRequested(type)
                _uiState.value = _uiState.value.copy(requestInFlight = type)
                _effects.send(PermissionEffect.RequestRuntimePermissions(runtimePermissions))
            }
        } else {
            viewModelScope.launch {
                _effects.send(PermissionEffect.OpenSettings(type))
            }
        }
    }

    private suspend fun statusFor(type: PermissionType, rationale: RationaleProvider): PermissionStatus {
        val runtimePermissions = permissionChecker.runtimePermissions(type)
        return resolver.resolve(
            isGranted = permissionChecker.isGranted(type),
            hasRuntimeRequest = runtimePermissions.isNotEmpty(),
            hasBeenRequested = historyStore.hasBeenRequested(type),
            shouldShowRationale = runtimePermissions.any(rationale::shouldShowRationale)
        )
    }
}
