package com.example.novari.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.novari.ui.components.LegalWebView
import com.example.novari.ui.screens.dashboard.DashboardScreen
import com.example.novari.ui.screens.onboarding.OnboardingRoute
import com.example.novari.ui.screens.permissions.SetupPermissionRoute
import com.example.novari.ui.screens.permissions.SetupPermissionScreen
import com.example.novari.ui.screens.settings.appearnce.AppearanceRoute
import com.example.novari.ui.screens.settings.support.SupportScreen
import com.example.novari.ui.screens.splash.SplashRoute
import com.example.novari.ui.theme.NovariMotion

private const val TRANSITION_DURATION_MS = 400
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
            SplashRoute(
                onNavigateToStart = { startDestination ->
                    navController.navigate(startDestination) {
                        // Removes Splash from the back stack so Back from
                        // the next screen does not return to it.
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
            OnboardingRoute(
                onOnboardingComplete = {
                    navController.navigate(Screen.Dashboard.route) {
                        // Onboarding is a one-time flow: remove it from the
                        // back stack so Back from Dashboard doesn't return to it.
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                }},
                onSkip = {

                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.Dashboard.route,
            enterTransition = { fadeIn(animationSpec = tween(TRANSITION_DURATION_MS)) }
        ) {
            DashboardScreen(
                onEnableAutoTracking = {
                    navController.navigate(Screen.SetupPermission.route)
                },
                onOpenPrivacyPolicy = {
                    navController.navigate(Screen.LegalDocument.createRoute(LegalDocType.PRIVACY_POLICY))
                },
                onOpenTerms = {
                    navController.navigate(Screen.LegalDocument.createRoute(LegalDocType.TERMS))
                },
                onAppearanceClick = {
                    navController.navigate(Screen.Appearance.route)
                },
                onContactUs = {
                   // navController.navigate(Screen.Contact.route)
                }
            )
        }

        composable(
            route = Screen.Appearance.route,
            enterTransition = { forwardEnter() },
            popEnterTransition = { backEnter() },
            popExitTransition = { backExit() }
        ) {
            AppearanceRoute(
                onBack = { navController.popBackStack() }
            )
        }

        composable(route = Screen.Contact.route) {
            SupportScreen(
                onBackClick = {}
            )
        }


        composable(
            route = Screen.SetupPermission.route,
            enterTransition = { fadeIn(animationSpec = tween(TRANSITION_DURATION_MS)) },
            exitTransition = { fadeOut(animationSpec = tween(TRANSITION_DURATION_MS)) }
        ) {
            SetupPermissionRoute(
                onClose = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.LegalDocument.route,
            arguments = listOf(
                navArgument(Screen.LegalDocument.ARG_DOC_TYPE) { type = NavType.StringType }
            ),
            enterTransition = { forwardEnter() },
            popExitTransition = { backExit() }
        ) { backStackEntry ->
            val docType = backStackEntry.arguments
                ?.getString(Screen.LegalDocument.ARG_DOC_TYPE)
                ?.let { LegalDocType.valueOf(it) }
                ?: LegalDocType.PRIVACY_POLICY

            LegalWebView(
                assetPath = docType.assetPath,
                title = docType.title,
                onBack = { navController.popBackStack() }
            )
        }
    }
}