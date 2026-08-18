package com.example.novari.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.novari.data.preferences.AppearancePreferences
import com.example.novari.data.preferences.DataStoreAppearancePreferences
import com.example.novari.data.preferences.DataStoreOnboardingPreferences
import com.example.novari.data.preferences.OnboardingPreferences
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.userPrefsDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

@Module
@InstallIn(SingletonComponent::class)
abstract class UserPreferencesModule {

    @Binds
    abstract fun bindOnboardingPreferences(
        impl: DataStoreOnboardingPreferences
    ): OnboardingPreferences

    @Binds
    abstract fun bindAppearancePreferences(
        impl: DataStoreAppearancePreferences
    ): AppearancePreferences

    @Module
    @InstallIn(SingletonComponent::class)
    object DataStoreProvider {
        @Provides
        @Singleton
        @UserPrefs
        fun provideUserPreferencesDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
            context.userPrefsDataStore
    }
}
