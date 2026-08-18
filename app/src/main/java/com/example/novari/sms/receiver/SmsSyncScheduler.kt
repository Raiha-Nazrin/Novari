package com.example.novari.sms.receiver

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

object SmsSyncScheduler {
    private const val WORK_NAME = "novari_sms_catch_up_sweep"

    fun enqueue(context: Context) {
        val request = OneTimeWorkRequestBuilder<SmsSyncWorker>().build()
        WorkManager.getInstance(context).enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.KEEP, request)
    }
}
