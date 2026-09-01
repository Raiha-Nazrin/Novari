package com.example.novari.sms.processor

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import com.example.novari.core.database.entity.SmsProcessingStatus
import com.example.novari.di.SmsPrefs
import com.example.novari.domain.repository.TransactionRepository
import com.example.novari.sms.repository.SmsProcessingRepository
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A parser or classifier fix does nothing for messages the catch-up sweep already marked
 * FAILED or IGNORED -- [SmsTransactionProcessor.process] short-circuits on a message's
 * fingerprint regardless of its stored status, so a previously-dropped SMS is otherwise never
 * looked at again.
 *
 * Bump [PARSER_VERSION] whenever [com.example.novari.sms.parser.FinancialSmsParser] changes in
 * a way that could turn a past FAILED message into a parseable one. Bump [CLASSIFIER_VERSION]
 * whenever [com.example.novari.sms.classifier.FinancialSmsClassifier] changes in a way that
 * could turn a past IGNORED message into a financial one -- a classifier fix needs to clear
 * IGNORED *and* FAILED, since nothing ever downgrades from IGNORED to re-attempt parsing.
 * On the next app start this clears the affected records and rewinds the sync watermark so the
 * regular catch-up sweep re-reads and reparses that window from the SMS content provider.
 */
@Singleton
class SmsReparseGate @Inject constructor(
    private val smsProcessingRepository: SmsProcessingRepository,
    private val transactionRepository: TransactionRepository,
    @SmsPrefs private val dataStore: DataStore<Preferences>
) {
    suspend fun reconcileIfNeeded() {
        val prefsSnapshot = dataStore.data.first()
        val appliedParserVersion = prefsSnapshot[APPLIED_PARSER_VERSION_KEY] ?: 0
        val appliedClassifierVersion = prefsSnapshot[APPLIED_CLASSIFIER_VERSION_KEY] ?: 0

        val classifierChanged = appliedClassifierVersion < CLASSIFIER_VERSION
        val parserChanged = appliedParserVersion < PARSER_VERSION
        if (!classifierChanged && !parserChanged) return

        if (classifierChanged) {
            smsProcessingRepository.deleteByStatus(SmsProcessingStatus.IGNORED)
        }
        if (classifierChanged || parserChanged) {
            smsProcessingRepository.deleteByStatus(SmsProcessingStatus.FAILED)
        }
        if (parserChanged) {
            reparseStaleProcessedRows()
        }

        val rewindTo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(REWIND_WINDOW_DAYS)
        dataStore.edit { prefs ->
            // Only rewind an existing watermark. A missing one means the sync worker has never
            // run yet (fresh install) -- HistoricalSmsImportGate owns that first backfill, and
            // there are no FAILED/IGNORED records from this device to recover.
            val lastProcessed = prefs[LAST_PROCESSED_KEY]
            if (lastProcessed != null && lastProcessed > rewindTo) {
                prefs[LAST_PROCESSED_KEY] = rewindTo
            }
            prefs[APPLIED_PARSER_VERSION_KEY] = PARSER_VERSION
            prefs[APPLIED_CLASSIFIER_VERSION_KEY] = CLASSIFIER_VERSION
        }
    }

    // A PROCESSED row whose derivedRevision predates the new PARSER_VERSION was parsed by a
    // less accurate parser. If its transaction is still exactly as the parser first produced it
    // (revision == 1L, never deleted), it's safe to soft-delete and let the rewound sweep
    // re-derive it -- an edit or a user delete means the row is no longer "untouched" and must
    // be left alone.
    private suspend fun reparseStaleProcessedRows() {
        for (row in smsProcessingRepository.findStaleProcessed(PARSER_VERSION)) {
            val transactionId = row.transactionId ?: continue
            val transaction = transactionRepository.findById(transactionId) ?: continue
            if (transaction.revision != 1L || transaction.deletedAt != null) continue
            transactionRepository.releaseForReparse(transaction)
            smsProcessingRepository.deleteById(row.id)
        }
    }

    companion object {
        // Bump on every parser change that can recover previously-FAILED messages, or that
        // fixes a mis-parse worth re-deriving on already-PROCESSED rows.
        const val PARSER_VERSION = 2

        // Bump on every classifier change that can recover previously-IGNORED messages.
        private const val CLASSIFIER_VERSION = 1
        private const val REWIND_WINDOW_DAYS = 90L
        private val APPLIED_PARSER_VERSION_KEY = intPreferencesKey("sms_reparse_applied_parser_version")
        private val APPLIED_CLASSIFIER_VERSION_KEY = intPreferencesKey("sms_reparse_applied_classifier_version")

        // Mirrors com.example.novari.sms.receiver.SmsSyncWorker.LAST_PROCESSED_KEY -- the sync
        // worker reads this same DataStore entry to decide where the next sweep starts from.
        private val LAST_PROCESSED_KEY = longPreferencesKey("sms_last_processed_timestamp")
    }
}
