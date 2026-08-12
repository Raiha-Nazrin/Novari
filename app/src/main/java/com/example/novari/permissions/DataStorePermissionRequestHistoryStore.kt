package com.example.novari.permissions

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStorePermissionRequestHistoryStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : PermissionRequestHistoryStore {

    override suspend fun hasBeenRequested(type: PermissionType): Boolean =
        dataStore.data.first()[key(type)] ?: false

    override suspend fun markRequested(type: PermissionType) {
        dataStore.edit { preferences -> preferences[key(type)] = true }
    }

    private fun key(type: PermissionType) = booleanPreferencesKey("requested_${type.name}")
}
