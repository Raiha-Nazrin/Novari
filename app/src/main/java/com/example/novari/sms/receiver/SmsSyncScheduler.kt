package com.example.novari.sms.receiver

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object SmsSyncScheduler {
    private const val WORK_NAME = "novari_sms_catch_up_sweep"
    private const val PERIODIC_WORK_NAME = "novari_sms_catch_up_sweep_periodic"
    private const val PERIODIC_INTERVAL_HOURS = 6L

    /** Runs once, immediately -- used on app start/boot so a sweep isn't stuck waiting on the periodic interval. */
    fun enqueue(context: Context) {
        val request = OneTimeWorkRequestBuilder<SmsSyncWorker>().build()
        WorkManager.getInstance(context).enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.KEEP, request)
    }

    /**
     * Safety net for the app never being foregrounded (and so never hitting
     * [MainActivity.onStart]) between a message arriving and the user checking their history.
     */
    fun enqueuePeriodic(context: Context) {
        val request = PeriodicWorkRequestBuilder<SmsSyncWorker>(PERIODIC_INTERVAL_HOURS, TimeUnit.HOURS).build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(PERIODIC_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
    }
}
