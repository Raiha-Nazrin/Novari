package com.example.novari.permissions

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import timber.log.Timber

object PermissionSettingsIntents {

    fun open(context: Context, type: PermissionType) {
        val intent = when {
            type == PermissionType.NOTIFICATIONS && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ->
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)

            else -> appDetailsIntent(context)
        }

        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Timber.w(e, "No activity found for %s settings intent, falling back to app details", type)
            context.startActivity(appDetailsIntent(context))
        }
    }

    private fun appDetailsIntent(context: Context): Intent =
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            .setData(Uri.fromParts("package", context.packageName, null))
}
