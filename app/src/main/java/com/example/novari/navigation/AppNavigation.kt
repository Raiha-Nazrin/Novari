package com.example.novari.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.novari.ui.screens.onboarding.OnboardingScreen
import com.example.novari.ui.screens.splash.SplashScreen


/**
 * Duration for the Splash -> Onboarding cross-fade.
 * Kept within the 300-500ms "smooth but not distracting" range.
 */
private const val TRANSITION_DURATION_MS = 400

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(
            route = Screen.Splash.route,
            exitTransition = { fadeOut(animationSpec = tween(TRANSITION_DURATION_MS)) }
        ) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(Screen.Onboarding.route) {
                        // Removes Splash from the back stack so Back from
                        // Onboarding does not return to it.
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.Onboarding.route,
            enterTransition = { fadeIn(animationSpec = tween(TRANSITION_DURATION_MS)) }
        ) {
            OnboardingScreen(
                onOnboardingComplete = {

                    // (e.g. Login/Home) once that screen exists.
                }
            )
        }
    }
}