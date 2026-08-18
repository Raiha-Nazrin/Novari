package com.example.novari.sms.receiver

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.novari.sms.di.SmsEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first

/**
 * Catch-up sweep, triggered on app foreground. Safety net for process death mid-`goAsync`,
 * the receiver not firing while the app is in Android's stopped state, and permission granted
 * while messages were arriving. Not the real-time path -- see [SmsBroadcastReceiver].
 */
class SmsSyncWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        return runCatching {
            val entryPoint = EntryPointAccessors.fromApplication(applicationContext, SmsEntryPoint::class.java)
            val dataStore = entryPoint.smsPrefsDataStore()
            val now = System.currentTimeMillis()
            val from = dataStore.data.first()[LAST_PROCESSED_KEY] ?: now

            if (from < now) {
                entryPoint.historicalSmsReader().process(from, now) { entryPoint.smsTransactionProcessor().process(it) }
            }
            dataStore.edit { it[LAST_PROCESSED_KEY] = now }
        }.fold({ Result.success() }, { Result.retry() })
    }

    companion object {
        val LAST_PROCESSED_KEY = longPreferencesKey("sms_last_processed_timestamp")
    }
}
