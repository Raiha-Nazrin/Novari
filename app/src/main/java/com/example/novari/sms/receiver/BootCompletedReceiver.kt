package com.example.novari.sms.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.novari.sms.permission.SmsPermissionState

/**
 * WorkManager re-schedules periodic work across reboots on its own, but that first periodic
 * run can be hours away. This closes the gap for a transaction SMS that arrives shortly after
 * boot, before the app has been opened.
 */
class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        if (!SmsPermissionState.canRead(context)) return
        SmsSyncScheduler.enqueue(context)
    }
}
