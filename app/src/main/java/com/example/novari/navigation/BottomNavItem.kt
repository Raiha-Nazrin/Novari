package com.example.novari.navigation


import androidx.annotation.DrawableRes
import com.example.novari.R


/**
 * Routes for the destinations nested inside DashboardScreen's own NavHost.
 *
 * Kept separate from [Screen] on purpose: [Screen] describes the app's
 * top-level flow (Splash -> Onboarding -> Dashboard), while this describes
 * the bottom-navigation destinations that live *inside* Dashboard. Nesting
 * them here avoids polluting the top-level sealed class with routes that
 * only make sense within the Dashboard container, while still following the
 * same sealed-class/route pattern already used by [Screen].
 *
 * Icons are drawable resources from the project's res/drawable folder
 * (not Material Icons) — replace the placeholder names below with your
 * actual resource names.
 */
sealed class BottomNavItem(
    val route: String,
    val label: String,
    @DrawableRes val iconRes: Int
) {
    data object Home : BottomNavItem("dashboard/home", "Home", R.drawable.ic_home)
    data object Insights : BottomNavItem("dashboard/insights", "Insights", R.drawable.ic_insights)
    data object Add : BottomNavItem("dashboard/add", "Add", R.drawable.ic_add)
    data object Search : BottomNavItem("dashboard/search", "Search", R.drawable.ic_search)
    data object Profile : BottomNavItem("dashboard/profile", "Profile", R.drawable.ic_profile)

    companion object {
        val items = listOf(Home, Insights, Add, Search, Profile)
    }
}