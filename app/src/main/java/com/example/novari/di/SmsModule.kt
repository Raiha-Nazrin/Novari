package com.example.novari.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.novari.domain.repository.MerchantCategoryRuleRepository
import com.example.novari.domain.repository.TransactionRepository
import com.example.novari.sms.classifier.FinancialSmsClassifier
import com.example.novari.sms.history.HistoricalSmsReader
import com.example.novari.sms.parser.FinancialSmsParser
import com.example.novari.sms.permission.AndroidSmsPermissionChecker
import com.example.novari.sms.permission.SmsPermissionChecker
import com.example.novari.sms.processor.SmsTransactionProcessor
import com.example.novari.sms.repository.SmsProcessingRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.smsDataStore: DataStore<Preferences> by preferencesDataStore(name = "sms_prefs")

@Module
@InstallIn(SingletonComponent::class)
object SmsModule {

    @Provides
    @Singleton
    @SmsPrefs
    fun provideSmsDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.smsDataStore

    @Provides
    @Singleton
    fun provideFinancialSmsClassifier(): FinancialSmsClassifier = FinancialSmsClassifier()

    @Provides
    @Singleton
    fun provideFinancialSmsParser(): FinancialSmsParser = FinancialSmsParser()

    @Provides
    @Singleton
    fun provideHistoricalSmsReader(@ApplicationContext context: Context): HistoricalSmsReader =
        HistoricalSmsReader(context.contentResolver)

    @Provides
    @Singleton
    fun provideSmsPermissionChecker(@ApplicationContext context: Context): SmsPermissionChecker =
        AndroidSmsPermissionChecker(context)

    @Provides
    @Singleton
    fun provideSmsTransactionProcessor(
        classifier: FinancialSmsClassifier,
        parser: FinancialSmsParser,
        transactionRepository: TransactionRepository,
        smsProcessingRepository: SmsProcessingRepository,
        merchantCategoryRuleRepository: MerchantCategoryRuleRepository
    ): SmsTransactionProcessor = SmsTransactionProcessor(
        classifier = classifier,
        parser = parser,
        transactionRepository = transactionRepository,
        smsProcessingRepository = smsProcessingRepository,
        merchantCategoryRuleRepository = merchantCategoryRuleRepository
    )
}
