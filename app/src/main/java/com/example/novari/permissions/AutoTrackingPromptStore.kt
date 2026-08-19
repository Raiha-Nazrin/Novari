package com.example.novari.permissions

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.example.novari.di.PermissionPrefs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tracks whether the user has ever opened the auto-tracking setup screen, so
 * HomeScreen's "Enable Auto Tracking" prompt can be hidden for good once
 * they've visited it — regardless of whether they granted the permission.
 */
@Singleton
class AutoTrackingPromptStore @Inject constructor(
    @PermissionPrefs private val dataStore: DataStore<Preferences>
) {
    val hasVisitedSetup: Flow<Boolean> = dataStore.data.map { it[SETUP_VISITED_KEY] ?: false }

    suspend fun markSetupVisited() {
        dataStore.edit { it[SETUP_VISITED_KEY] = true }
    }

    private companion object {
        val SETUP_VISITED_KEY = booleanPreferencesKey("auto_tracking_setup_visited")
    }
}
