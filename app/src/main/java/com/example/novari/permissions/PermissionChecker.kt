package com.example.novari.permissions

interface PermissionChecker {

    /** Runtime permissions that must be requested for [type]. Empty means the only path is Settings. */
    fun runtimePermissions(type: PermissionType): List<String>

    /** Whether [type] is actually enabled right now, per the live OS state. */
    fun isGranted(type: PermissionType): Boolean
}
