package com.example.novari.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.novari.core.database.DatabaseProvider
import com.example.novari.core.database.NovariDatabase
import com.example.novari.core.database.dao.CategoryDao
import com.example.novari.core.database.dao.SmsProcessingDao
import com.example.novari.core.database.dao.TransactionDao
import com.example.novari.core.security.DatabaseKeyProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.runBlocking
import javax.inject.Singleton

private val Context.securityDataStore: DataStore<Preferences> by preferencesDataStore(name = "security_prefs")

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    @SecurityPrefs
    fun provideSecurityDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.securityDataStore

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
        keyProvider: DatabaseKeyProvider,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): NovariDatabase {
        val databasePassword = runBlocking(ioDispatcher) { keyProvider.getOrCreateKey() }
        return DatabaseProvider.create(context, databasePassword)
    }

    @Provides
    fun provideTransactionDao(database: NovariDatabase): TransactionDao =
        database.transactionDao()

    @Provides
    fun provideCategoryDao(database: NovariDatabase): CategoryDao =
        database.categoryDao()

    @Provides
    fun provideSmsProcessingDao(database: NovariDatabase): SmsProcessingDao =
        database.smsProcessingDao()
}
