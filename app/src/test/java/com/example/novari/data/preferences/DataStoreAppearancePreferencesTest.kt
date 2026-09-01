package com.example.novari.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.novari.ui.theme.AccentColor
import com.example.novari.ui.theme.ThemeMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class DataStoreAppearancePreferencesTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private fun newDataStore(
        file: java.io.File = temporaryFolder.newFile("test_user_prefs.preferences_pb")
    ): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            scope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher()),
            produceFile = { file }
        )

    @Test
    fun `defaults to Light and Teal`() = runTest {
        val preferences = DataStoreAppearancePreferences(newDataStore())

        val settings = preferences.settings.first()

        assertEquals(ThemeMode.LIGHT, settings.theme)
        assertEquals(AccentColor.TEAL, settings.accent)
    }

    @Test
    fun `setThemeMode round-trips`() = runTest {
        val preferences = DataStoreAppearancePreferences(newDataStore())

        preferences.setThemeMode(ThemeMode.DARK)

        assertEquals(ThemeMode.DARK, preferences.settings.first().theme)
    }

    @Test
    fun `setAccent round-trips`() = runTest {
        val preferences = DataStoreAppearancePreferences(newDataStore())

        preferences.setAccent(AccentColor.PURPLE)

        assertEquals(AccentColor.PURPLE, preferences.settings.first().accent)
    }

    @Test
    fun `unknown stored enum value falls back to default`() = runTest {
        val dataStore = newDataStore()
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey("theme_mode")] = "SYSTEM"
            preferences[stringPreferencesKey("accent_color")] = "MAROON"
        }
        val preferences = DataStoreAppearancePreferences(dataStore)

        val settings = preferences.settings.first()

        assertEquals(ThemeMode.LIGHT, settings.theme)
        assertEquals(AccentColor.TEAL, settings.accent)
    }
}
