package com.example.novari.sms.receiver

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.novari.sms.di.SmsEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

/**
 * Catch-up sweep, triggered on app foreground. Safety net for process death mid-`goAsync`,
 * the receiver not firing while the app is in Android's stopped state, and permission granted
 * while messages were arriving. Not the real-time path -- see [SmsBroadcastReceiver].
 */
class SmsSyncWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        return runCatching {
            val entryPoint = EntryPointAccessors.fromApplication(applicationContext, SmsEntryPoint::class.java)
            entryPoint.smsReparseGate().reconcileIfNeeded()

            val dataStore = entryPoint.smsPrefsDataStore()
            val now = System.currentTimeMillis()
            val from = dataStore.data.first()[LAST_PROCESSED_KEY] ?: now

            if (from < now) {
                entryPoint.historicalSmsReader().process(from, now) { entryPoint.smsTransactionProcessor().process(it) }
            }
            // Advancing the watermark to exactly `now` would permanently skip a provider row
            // whose insert lands after this query runs but is dated before `now`. Re-scanning
            // the overlap window is cheap -- the fingerprint insert-ignore already makes
            // reprocessing idempotent -- so leave a trailing margin instead.
            dataStore.edit {
                it[LAST_PROCESSED_KEY] = now - WATERMARK_OVERLAP_MILLIS
                // Distinct from LAST_PROCESSED_KEY (a rewindable cursor): this is a one-way
                // "detection is alive" signal for the health screen/banner, so a reparse
                // rewind doesn't make the app look like it stopped sweeping.
                it[LAST_SWEEP_SUCCESS_KEY] = now
            }
        }.fold({ Result.success() }, { Result.retry() })
    }

    companion object {
        val LAST_PROCESSED_KEY = longPreferencesKey("sms_last_processed_timestamp")
        val LAST_SWEEP_SUCCESS_KEY = longPreferencesKey("sms_last_successful_sweep_at")
        private val WATERMARK_OVERLAP_MILLIS = TimeUnit.MINUTES.toMillis(10)
    }
}
