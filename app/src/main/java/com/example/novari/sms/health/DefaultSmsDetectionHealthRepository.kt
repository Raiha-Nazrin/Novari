package com.example.novari.sms.health

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import com.example.novari.di.SmsPrefs
import com.example.novari.sms.repository.SmsProcessingRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class DefaultSmsDetectionHealthRepository @Inject constructor(
    private val smsProcessingRepository: SmsProcessingRepository,
    @SmsPrefs private val dataStore: DataStore<Preferences>
) : SmsDetectionHealthRepository {

    override fun observeProcessedCount(): Flow<Int> = smsProcessingRepository.observeProcessedCount()

    override fun observeIgnoredCount(): Flow<Int> = smsProcessingRepository.observeSilentlyIgnoredCount()

    override fun observeLastSuccessfulSweepAt(): Flow<Long?> =
        dataStore.data.map { it[LAST_SWEEP_SUCCESS_KEY] }

    companion object {
        // Mirrors com.example.novari.sms.receiver.SmsSyncWorker.LAST_SWEEP_SUCCESS_KEY --
        // same convention as SmsReparseGate's mirrored LAST_PROCESSED_KEY, to avoid a
        // dependency from sms.health back into the receiver package for one constant.
        private val LAST_SWEEP_SUCCESS_KEY = longPreferencesKey("sms_last_successful_sweep_at")
    }
}
