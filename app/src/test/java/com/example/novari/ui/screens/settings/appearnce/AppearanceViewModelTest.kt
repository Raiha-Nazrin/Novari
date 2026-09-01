package com.example.novari.ui.screens.settings.appearnce

import com.example.novari.data.preferences.AppearancePreferences
import com.example.novari.ui.theme.AccentColor
import com.example.novari.ui.theme.AppearanceSettings
import com.example.novari.ui.theme.ThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppearanceViewModelTest {

    private val dispatcher = StandardTestDispatcher()

    private class FakeAppearancePreferences(
        initial: AppearanceSettings = AppearanceSettings()
    ) : AppearancePreferences {
        val state = MutableStateFlow(initial)
        override val settings = state
        override suspend fun setThemeMode(mode: ThemeMode) {
            state.value = state.value.copy(theme = mode)
        }
        override suspend fun setAccent(accent: AccentColor) {
            state.value = state.value.copy(accent = accent)
        }
        override suspend fun setSettings(settings: AppearanceSettings) {
            state.value = settings
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state mirrors the preferences flow with no unsaved changes`() = runTest {
        val preferences = FakeAppearancePreferences()
        val viewModel = AppearanceViewModel(preferences)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(AppearanceSettings(), viewModel.uiState.value.draft)
        assertEquals(AppearanceSettings(), viewModel.uiState.value.saved)
        assertFalse(viewModel.uiState.value.hasUnsavedChanges)
    }

    @Test
    fun `onThemeSelected updates draft but does not persist`() = runTest {
        val preferences = FakeAppearancePreferences()
        val viewModel = AppearanceViewModel(preferences)
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.onThemeSelected(ThemeMode.DARK)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(ThemeMode.DARK, viewModel.uiState.value.draft.theme)
        assertEquals(ThemeMode.LIGHT, preferences.state.value.theme)
        assertTrue(viewModel.uiState.value.hasUnsavedChanges)
    }

    @Test
    fun `onAccentSelected updates draft but does not persist`() = runTest {
        val preferences = FakeAppearancePreferences()
        val viewModel = AppearanceViewModel(preferences)
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.onAccentSelected(AccentColor.BLUE)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(AccentColor.BLUE, viewModel.uiState.value.draft.accent)
        assertEquals(AccentColor.TEAL, preferences.state.value.accent)
        assertTrue(viewModel.uiState.value.hasUnsavedChanges)
    }

    @Test
    fun `save persists draft and clears hasUnsavedChanges`() = runTest {
        val preferences = FakeAppearancePreferences()
        val viewModel = AppearanceViewModel(preferences)
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.onThemeSelected(ThemeMode.DARK)
        viewModel.onAccentSelected(AccentColor.PURPLE)
        viewModel.save()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(ThemeMode.DARK, preferences.state.value.theme)
        assertEquals(AccentColor.PURPLE, preferences.state.value.accent)
        assertFalse(viewModel.uiState.value.hasUnsavedChanges)
    }

    @Test
    fun `discard reverts draft to saved`() = runTest {
        val preferences = FakeAppearancePreferences()
        val viewModel = AppearanceViewModel(preferences)
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.onThemeSelected(ThemeMode.DARK)
        viewModel.discard()
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(ThemeMode.LIGHT, viewModel.uiState.value.draft.theme)
        assertFalse(viewModel.uiState.value.hasUnsavedChanges)
    }

    @Test
    fun `selecting back to the saved value clears hasUnsavedChanges`() = runTest {
        val preferences = FakeAppearancePreferences()
        val viewModel = AppearanceViewModel(preferences)
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.onThemeSelected(ThemeMode.DARK)
        viewModel.onThemeSelected(ThemeMode.LIGHT)
        dispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.hasUnsavedChanges)
    }

    @Test
    fun `save emits Saved event once`() = runTest {
        val preferences = FakeAppearancePreferences()
        val viewModel = AppearanceViewModel(preferences)
        dispatcher.scheduler.advanceUntilIdle()

        viewModel.onThemeSelected(ThemeMode.DARK)
        viewModel.save()
        dispatcher.scheduler.advanceUntilIdle()

        val event = viewModel.eventFlow.first()
        assertEquals(AppearanceEvent.Saved, event)
    }
}
