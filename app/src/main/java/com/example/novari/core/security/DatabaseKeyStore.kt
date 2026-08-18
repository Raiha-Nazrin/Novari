package com.example.novari.core.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Android Keystore boundary for protecting the random SQLCipher key.
 *
 * Production implementation should persist WrappedDatabaseKey in app-private
 * storage (for example DataStore). The raw database key must never be hardcoded.
 */
@Singleton
class DatabaseKeyStore @Inject constructor() {

    companion object {
        private const val KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "novari_database_wrapping_key"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val TAG_SIZE_BITS = 128
    }

    fun createDatabaseKey(): ByteArray =
        ByteArray(32).also { SecureRandom().nextBytes(it) }

    fun wrap(databaseKey: ByteArray): WrappedDatabaseKey {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateWrappingKey())

        return try {
            WrappedDatabaseKey(
                iv = cipher.iv,
                ciphertext = cipher.doFinal(databaseKey)
            )
        } finally {
            databaseKey.fill(0)
        }
    }

    fun unwrap(wrapped: WrappedDatabaseKey): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(
            Cipher.DECRYPT_MODE,
            getOrCreateWrappingKey(),
            GCMParameterSpec(TAG_SIZE_BITS, wrapped.iv)
        )
        return cipher.doFinal(wrapped.ciphertext)
    }

    private fun getOrCreateWrappingKey(): SecretKey {
        val store = KeyStore.getInstance(KEYSTORE).apply { load(null) }

        if (store.containsAlias(KEY_ALIAS)) {
            return store.getKey(KEY_ALIAS, null) as SecretKey
        }

        return KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            KEYSTORE
        ).apply {
            init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setUserAuthenticationRequired(false)
                    .build()
            )
        }.generateKey()
    }
}

data class WrappedDatabaseKey(
    val iv: ByteArray,
    val ciphertext: ByteArray
)
