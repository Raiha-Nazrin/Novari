package com.example.novari.permissions

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class AutoTrackingPromptStoreTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private fun newDataStore(file: File): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(produceFile = { file })

    @Test
    fun `defaults to not visited`() = runTest {
        val store = AutoTrackingPromptStore(newDataStore(tempFolder.newFile("prefs1.preferences_pb")))

        assertFalse(store.hasVisitedSetup.first())
    }

    @Test
    fun `marking visited flips the flag`() = runTest {
        val store = AutoTrackingPromptStore(newDataStore(tempFolder.newFile("prefs2.preferences_pb")))

        store.markSetupVisited()

        assertTrue(store.hasVisitedSetup.first())
    }
}
