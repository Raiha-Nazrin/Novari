package com.example.novari.data.preferences

import kotlinx.coroutines.flow.Flow

interface OnboardingPreferences {
    val hasCompletedOnboarding: Flow<Boolean>
    suspend fun setCompleted()
    suspend fun reset()
}
