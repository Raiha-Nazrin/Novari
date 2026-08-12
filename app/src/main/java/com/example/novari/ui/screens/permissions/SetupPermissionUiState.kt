package com.example.novari.ui.screens.permissions

import androidx.compose.runtime.Immutable
import com.example.novari.permissions.PermissionStatus
import com.example.novari.permissions.PermissionType

@Immutable
data class SetupPermissionUiState(
    val sms: PermissionStatus = PermissionStatus.NOT_REQUESTED,
    val notifications: PermissionStatus = PermissionStatus.NOT_REQUESTED,
    val requestInFlight: PermissionType? = null
)

sealed interface PermissionEffect {
    data class RequestRuntimePermissions(val permissions: List<String>) : PermissionEffect
    data class OpenSettings(val type: PermissionType) : PermissionEffect
}
