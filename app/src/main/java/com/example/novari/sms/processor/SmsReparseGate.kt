package com.example.novari.sms.processor

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import com.example.novari.core.database.entity.SmsProcessingStatus
import com.example.novari.di.SmsPrefs
import com.example.novari.sms.repository.SmsProcessingRepository
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A parser fix does nothing for messages the catch-up sweep already marked FAILED --
 * [SmsTransactionProcessor.process] short-circuits on a message's fingerprint regardless of
 * its stored status, so a previously-unparseable SMS is otherwise never looked at again.
 *
 * Bump [PARSER_VERSION] whenever [com.example.novari.sms.parser.FinancialSmsParser] changes
 * in a way that could turn a past FAILED message into a parseable one. On the next app start
 * this clears the FAILED records and rewinds the sync watermark so the regular catch-up sweep
 * re-reads and re-parses that window from the SMS content provider.
 */
@Singleton
class SmsReparseGate @Inject constructor(
    private val smsProcessingRepository: SmsProcessingRepository,
    @SmsPrefs private val dataStore: DataStore<Preferences>
) {
    suspend fun reconcileIfNeeded() {
        val appliedVersion = dataStore.data.first()[APPLIED_VERSION_KEY] ?: 0
        if (appliedVersion >= PARSER_VERSION) return

        smsProcessingRepository.deleteByStatus(SmsProcessingStatus.FAILED)

        val rewindTo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(REWIND_WINDOW_DAYS)
        dataStore.edit { prefs ->
            // Only rewind an existing watermark. A missing one means the sync worker has never
            // run yet (fresh install) -- HistoricalSmsImportGate owns that first backfill, and
            // there are no FAILED records from this device to recover.
            val lastProcessed = prefs[LAST_PROCESSED_KEY]
            if (lastProcessed != null && lastProcessed > rewindTo) {
                prefs[LAST_PROCESSED_KEY] = rewindTo
            }
            prefs[APPLIED_VERSION_KEY] = PARSER_VERSION
        }
    }

    private companion object {
        // Bump on every parser change that can recover previously-FAILED messages.
        const val PARSER_VERSION = 2
        const val REWIND_WINDOW_DAYS = 90L
        val APPLIED_VERSION_KEY = intPreferencesKey("sms_reparse_applied_version")

        // Mirrors com.example.novari.sms.receiver.SmsSyncWorker.LAST_PROCESSED_KEY -- the sync
        // worker reads this same DataStore entry to decide where the next sweep starts from.
        val LAST_PROCESSED_KEY = longPreferencesKey("sms_last_processed_timestamp")
    }
}
