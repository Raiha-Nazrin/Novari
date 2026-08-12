package com.example.novari.permissions

import javax.inject.Inject

/**
 * `shouldShowRequestPermissionRationale` returns `false` both before the first request and
 * after a permanent denial, so [hasBeenRequested] must come from app-persisted history —
 * the OS has no way to distinguish those two cases on its own.
 */
class PermissionStatusResolver @Inject constructor() {

    fun resolve(
        isGranted: Boolean,
        hasRuntimeRequest: Boolean,
        hasBeenRequested: Boolean,
        shouldShowRationale: Boolean
    ): PermissionStatus = when {
        isGranted -> PermissionStatus.GRANTED
        !hasRuntimeRequest -> PermissionStatus.PERMANENTLY_DENIED
        !hasBeenRequested -> PermissionStatus.NOT_REQUESTED
        shouldShowRationale -> PermissionStatus.DENIED
        else -> PermissionStatus.PERMANENTLY_DENIED
    }
}
