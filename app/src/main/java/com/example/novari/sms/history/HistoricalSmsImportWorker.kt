package com.example.novari.sms.history

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.novari.sms.di.SmsEntryPoint
import dagger.hilt.android.EntryPointAccessors

class HistoricalSmsImportWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val start = inputData.getLong(KEY_START, Long.MIN_VALUE)
        val end = inputData.getLong(KEY_END, Long.MIN_VALUE)
        if (start == Long.MIN_VALUE || end == Long.MIN_VALUE || start > end) return Result.failure()

        return runCatching {
            val entryPoint = EntryPointAccessors.fromApplication(applicationContext, SmsEntryPoint::class.java)
            entryPoint.historicalSmsReader().process(start, end) { entryPoint.smsTransactionProcessor().process(it) }
        }.fold({ Result.success() }, { Result.retry() })
    }
    companion object { const val KEY_START = "historical_start"; const val KEY_END = "historical_end" }
}
