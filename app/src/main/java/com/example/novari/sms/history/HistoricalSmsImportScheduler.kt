package com.example.novari.sms.history

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

object HistoricalSmsImportScheduler {
    private const val WORK_NAME = "novari_historical_sms_import"
    fun enqueue(context: Context, startTimestamp: Long, endTimestamp: Long) {
        require(startTimestamp <= endTimestamp)
        val request = OneTimeWorkRequestBuilder<HistoricalSmsImportWorker>()
            .setInputData(workDataOf(HistoricalSmsImportWorker.KEY_START to startTimestamp, HistoricalSmsImportWorker.KEY_END to endTimestamp))
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.NOT_REQUIRED).build())
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(WORK_NAME, ExistingWorkPolicy.KEEP, request)
    }
}
