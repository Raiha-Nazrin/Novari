package com.example.novari.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class DataStoreOnboardingPreferencesTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private fun newDataStore(
        file: java.io.File = temporaryFolder.newFile("test_user_prefs.preferences_pb"),
        scope: CoroutineScope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher())
    ): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            scope = scope,
            produceFile = { file }
        )

    @Test
    fun `defaults to not completed`() = runTest {
        val preferences = DataStoreOnboardingPreferences(newDataStore())

        assertFalse(preferences.hasCompletedOnboarding.first())
    }

    @Test
    fun `setCompleted flips the flag to true`() = runTest {
        val preferences = DataStoreOnboardingPreferences(newDataStore())

        preferences.setCompleted()

        assertTrue(preferences.hasCompletedOnboarding.first())
    }

    @Test
    fun `completion survives a new store instance over the same file`() = runTest {
        val file = temporaryFolder.newFile("shared_user_prefs.preferences_pb")
        val firstScope = CoroutineScope(SupervisorJob() + UnconfinedTestDispatcher())
        DataStoreOnboardingPreferences(newDataStore(file, firstScope)).setCompleted()
        firstScope.cancel()

        val reopened = DataStoreOnboardingPreferences(newDataStore(file))

        assertEquals(true, reopened.hasCompletedOnboarding.first())
    }

    @Test
    fun `reset flips the flag back to false`() = runTest {
        val preferences = DataStoreOnboardingPreferences(newDataStore())
        preferences.setCompleted()

        preferences.reset()

        assertFalse(preferences.hasCompletedOnboarding.first())
    }
}
