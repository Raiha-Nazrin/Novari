package com.example.novari.navigation

//centralized navigation destination
sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
}