package com.example.novari.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidPermissionChecker @Inject constructor(
    @ApplicationContext private val context: Context
) : PermissionChecker {

    override fun runtimePermissions(type: PermissionType): List<String> = when (type) {
        PermissionType.SMS -> listOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS)
        PermissionType.NOTIFICATIONS -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            emptyList()
        }
    }

    override fun isGranted(type: PermissionType): Boolean = when (type) {
        PermissionType.SMS -> runtimePermissions(type).all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
        PermissionType.NOTIFICATIONS -> NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
}
