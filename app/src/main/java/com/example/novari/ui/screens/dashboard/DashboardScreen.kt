package com.example.novari.ui.screens.dashboard

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.novari.navigation.BottomNavItem
import com.example.novari.ui.screens.AddExpenseScreen
import com.example.novari.ui.screens.home.HomeScreen
import com.example.novari.ui.screens.home.mockHomeUiState
import com.example.novari.ui.screens.insights.InsightsScreen
import com.example.novari.ui.screens.search.SearchScreen
import com.example.novari.ui.screens.settings.SettingsScreen
import com.example.novari.ui.screens.transactions.AddTransactionScreen


/**
 * Main container shown after onboarding. Owns the bottom navigation bar and
 * a NavHost scoped to Dashboard's own destinations (Home, Insights, Add,
 * Search, Profile).
 *
 * This NavHost is intentionally separate from the app-level one in
 * AppNavigation.kt: the outer NavHost only needs to know Dashboard exists
 * as a single destination; switching between bottom-nav tabs is an internal
 * concern of Dashboard and has no bearing on the Splash/Onboarding/Dashboard
 * back stack.
 */
@Composable
fun DashboardScreen(
    onEnableAutoTracking: () -> Unit = {},
    onOpenPrivacyPolicy: () -> Unit = {},
    onOpenTerms: () -> Unit = {},
    dashboardNavController: NavHostController = rememberNavController()
) {
    val currentBackStackEntry by dashboardNavController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                currentRoute = currentRoute,
                onItemSelected = { item ->
                    dashboardNavController.navigate(item.route) {
                        // Avoid piling up duplicate destinations on repeated taps
                        // and keep each tab's own back stack when returning to it.
                        popUpTo(dashboardNavController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = dashboardNavController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavItem.Home.route) {
                HomeScreen(
                    uiState = mockHomeUiState,
                    onEnableAutoTracking = /* wire to future auto-tracking flow */
                        onEnableAutoTracking,
                    onAddExpenseManually = { /*  navigate to Add Expense flow */ },
                    onTransactionClick = { /* : expand/collapse handled internally for now */ },
                    onEditTransaction = { /*  wire to future edit flow */ },
                    onDeleteTransaction = { /*  wire to future delete flow */ },
                    onSeeAllTransactions = { /*  navigate to a full transactions screen */ }
                )
            }
            composable(BottomNavItem.Insights.route) {
                InsightsScreen()
            }
            composable(BottomNavItem.Add.route) {
                AddTransactionScreen(
                    onNavigateBack = {}
                )
            }
            composable(BottomNavItem.Search.route) {
                SearchScreen()
            }
            composable(BottomNavItem.Profile.route) {
                SettingsScreen(
                    onPrivacyPolicyClick = onOpenPrivacyPolicy,
                    onTermsClick = onOpenTerms
                )
            }
        }
    }
}