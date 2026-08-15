package com.example.novari.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.novari.ui.screens.onboarding.OnboardingScreen
import com.example.novari.ui.screens.splash.SplashScreen
import com.example.novari.ui.theme.NovariMotion

private fun forwardEnter(): EnterTransition =
    slideInVertically(animationSpec = NovariMotion.Offset) { it / 6 } +
        fadeIn(animationSpec = NovariMotion.Float)

private fun forwardExit(): ExitTransition =
    slideOutVertically(animationSpec = NovariMotion.Offset) { -it / 6 } +
        fadeOut(animationSpec = NovariMotion.Float)

private fun backEnter(): EnterTransition =
    slideInVertically(animationSpec = NovariMotion.Offset) { -it / 6 } +
        fadeIn(animationSpec = NovariMotion.Float)

private fun backExit(): ExitTransition =
    slideOutVertically(animationSpec = NovariMotion.Offset) { it / 6 } +
        fadeOut(animationSpec = NovariMotion.Float)

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
            // Nothing to push against, and it is the app's first frame: a pure fade suffices.
            exitTransition = { fadeOut(animationSpec = NovariMotion.Float) }
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
            enterTransition = { forwardEnter() },
            popEnterTransition = { backEnter() },
            popExitTransition = { backExit() }
        ) {
            OnboardingScreen(
                onOnboardingComplete = {
                },
                onSkip = {
                }
            )
        }
    }
}