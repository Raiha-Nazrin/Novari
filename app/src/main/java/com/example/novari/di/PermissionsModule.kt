package com.example.novari.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.novari.permissions.AndroidPermissionChecker
import com.example.novari.permissions.DataStorePermissionRequestHistoryStore
import com.example.novari.permissions.PermissionChecker
import com.example.novari.permissions.PermissionRequestHistoryStore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.permissionDataStore: DataStore<Preferences> by preferencesDataStore(name = "permission_prefs")

@Module
@InstallIn(SingletonComponent::class)
abstract class PermissionsModule {

    @Binds
    abstract fun bindPermissionChecker(impl: AndroidPermissionChecker): PermissionChecker

    @Binds
    abstract fun bindPermissionRequestHistoryStore(
        impl: DataStorePermissionRequestHistoryStore
    ): PermissionRequestHistoryStore

    @Module
    @InstallIn(SingletonComponent::class)
    object DataStoreProvider {
        @Provides
        @Singleton
        fun providePermissionDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
            context.permissionDataStore
    }
}
