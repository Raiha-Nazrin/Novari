package com.example.novari.permissions

interface PermissionRequestHistoryStore {
    suspend fun hasBeenRequested(type: PermissionType): Boolean
    suspend fun markRequested(type: PermissionType)
}
