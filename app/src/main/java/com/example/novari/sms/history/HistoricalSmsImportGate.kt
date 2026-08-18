package com.example.novari.sms.history

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.example.novari.di.SmsPrefs
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Runs the historical backfill once, ever, after SMS permission is granted.
 * `ExistingWorkPolicy.KEEP` alone is insufficient: WorkManager prunes finished work, after
 * which `KEEP` would re-enqueue on every subsequent grant-transition check.
 */
@Singleton
class HistoricalSmsImportGate @Inject constructor(
    @ApplicationContext private val context: Context,
    @SmsPrefs private val dataStore: DataStore<Preferences>
) {
    suspend fun triggerIfNeeded() {
        val alreadyCompleted = dataStore.data.first()[COMPLETED_KEY] ?: false
        if (alreadyCompleted) return

        dataStore.edit { it[COMPLETED_KEY] = true }

        val end = System.currentTimeMillis()
        val start = end - TimeUnit.DAYS.toMillis(BACKFILL_WINDOW_DAYS)
        HistoricalSmsImportScheduler.enqueue(context, start, end)
    }

    private companion object {
        val COMPLETED_KEY = booleanPreferencesKey("historical_import_completed")
        const val BACKFILL_WINDOW_DAYS = 90L
    }
}
