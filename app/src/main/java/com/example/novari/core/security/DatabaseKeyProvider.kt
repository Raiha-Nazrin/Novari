package com.example.novari.core.security

import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.novari.di.SecurityPrefs
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Owns the lifecycle of the SQLCipher database key: creates it once, wraps it with
 * [DatabaseKeyStore], and persists only the wrapped form. The raw key is handed out
 * on demand and never stored.
 */
@Singleton
class DatabaseKeyProvider @Inject constructor(
    private val keyStore: DatabaseKeyStore,
    @SecurityPrefs private val dataStore: DataStore<Preferences>
) {
    private val mutex = Mutex()

    suspend fun getOrCreateKey(): ByteArray = mutex.withLock {
        val preferences = dataStore.data.first()
        val existingIv = preferences[IV_KEY]
        val existingCiphertext = preferences[CIPHERTEXT_KEY]

        if (existingIv != null && existingCiphertext != null) {
            return@withLock keyStore.unwrap(
                WrappedDatabaseKey(
                    iv = Base64.decode(existingIv, Base64.NO_WRAP),
                    ciphertext = Base64.decode(existingCiphertext, Base64.NO_WRAP)
                )
            )
        }

        val newKey = keyStore.createDatabaseKey()
        val keyForDatabase = newKey.copyOf()
        val wrapped = keyStore.wrap(newKey)

        dataStore.edit { prefs ->
            prefs[IV_KEY] = Base64.encodeToString(wrapped.iv, Base64.NO_WRAP)
            prefs[CIPHERTEXT_KEY] = Base64.encodeToString(wrapped.ciphertext, Base64.NO_WRAP)
        }

        keyForDatabase
    }

    private companion object {
        val IV_KEY = stringPreferencesKey("database_key_iv")
        val CIPHERTEXT_KEY = stringPreferencesKey("database_key_ciphertext")
    }
}
