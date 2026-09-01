package com.example.novari.sms.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.example.novari.di.SmsPrefs
import com.example.novari.sms.history.HistoricalSmsReader
import com.example.novari.sms.processor.SmsReparseGate
import com.example.novari.sms.processor.SmsTransactionProcessor
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SmsEntryPoint {
    fun smsTransactionProcessor(): SmsTransactionProcessor
    fun historicalSmsReader(): HistoricalSmsReader
    fun smsReparseGate(): SmsReparseGate

    @SmsPrefs
    fun smsPrefsDataStore(): DataStore<Preferences>
}
