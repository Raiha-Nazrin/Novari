package com.example.novari.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.example.novari.di.UserPrefs
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreOnboardingPreferences @Inject constructor(
    @UserPrefs private val dataStore: DataStore<Preferences>
) : OnboardingPreferences {

    private val hasCompletedKey = booleanPreferencesKey("has_completed_onboarding")

    override val hasCompletedOnboarding = dataStore.data
        .catch { e ->
            if (e is IOException) {
                Timber.e(e, "Failed to read onboarding preferences, defaulting to incomplete")
                emit(emptyPreferences())
            } else {
                throw e
            }
        }
        .map { preferences -> preferences[hasCompletedKey] ?: false }

    override suspend fun setCompleted() {
        dataStore.edit { preferences -> preferences[hasCompletedKey] = true }
    }

    override suspend fun reset() {
        dataStore.edit { preferences -> preferences[hasCompletedKey] = false }
    }
}
