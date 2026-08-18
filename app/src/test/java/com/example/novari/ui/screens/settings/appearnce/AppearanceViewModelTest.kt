package com.example.novari.ui.screens.settings.appearnce

import com.example.novari.data.preferences.AppearancePreferences
import com.example.novari.ui.theme.AccentColor
import com.example.novari.ui.theme.AppearanceSettings
import com.example.novari.ui.theme.ThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
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
    fun `settings mirrors the preferences flow`() = runTest {
        val preferences = FakeAppearancePreferences()
        val viewModel = AppearanceViewModel(preferences)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(AppearanceSettings(), viewModel.settings.value)
    }

    @Test
    fun `onThemeSelected persists the new theme`() = runTest {
        val preferences = FakeAppearancePreferences()
        val viewModel = AppearanceViewModel(preferences)

        viewModel.onThemeSelected(ThemeMode.DARK)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(ThemeMode.DARK, viewModel.settings.value.theme)
    }

    @Test
    fun `onAccentSelected persists the new accent`() = runTest {
        val preferences = FakeAppearancePreferences()
        val viewModel = AppearanceViewModel(preferences)

        viewModel.onAccentSelected(AccentColor.BLUE)
        dispatcher.scheduler.advanceUntilIdle()

        assertEquals(AccentColor.BLUE, viewModel.settings.value.accent)
    }
}
