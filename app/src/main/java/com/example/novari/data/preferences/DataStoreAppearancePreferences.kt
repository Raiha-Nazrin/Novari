package com.example.novari.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.novari.di.UserPrefs
import com.example.novari.ui.theme.AccentColor
import com.example.novari.ui.theme.AppearanceSettings
import com.example.novari.ui.theme.ThemeMode
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreAppearancePreferences @Inject constructor(
    @UserPrefs private val dataStore: DataStore<Preferences>
) : AppearancePreferences {

    private val themeModeKey = stringPreferencesKey("theme_mode")
    private val accentColorKey = stringPreferencesKey("accent_color")

    override val settings = dataStore.data
        .catch { e ->
            if (e is IOException) {
                Timber.e(e, "Failed to read appearance preferences, defaulting to Light/Teal")
                emit(emptyPreferences())
            } else {
                throw e
            }
        }
        .map { preferences ->
            AppearanceSettings(
                theme = preferences[themeModeKey].toEnumOrDefault(ThemeMode.LIGHT),
                accent = preferences[accentColorKey].toEnumOrDefault(AccentColor.TEAL)
            )
        }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { preferences -> preferences[themeModeKey] = mode.name }
    }

    override suspend fun setAccent(accent: AccentColor) {
        dataStore.edit { preferences -> preferences[accentColorKey] = accent.name }
    }

    override suspend fun setSettings(settings: AppearanceSettings) {
        dataStore.edit { preferences ->
            preferences[themeModeKey] = settings.theme.name
            preferences[accentColorKey] = settings.accent.name
        }
    }

    private inline fun <reified T : Enum<T>> String?.toEnumOrDefault(default: T): T =
        this?.let { raw -> runCatching { enumValueOf<T>(raw) }.getOrDefault(default) } ?: default
}
