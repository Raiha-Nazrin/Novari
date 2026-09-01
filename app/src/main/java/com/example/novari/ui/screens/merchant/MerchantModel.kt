package com.example.novari.ui.screens.merchant

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color

/**
 * A selectable merchant. [logoRes] is optional — when a real brand-logo
 * drawable isn't available yet, [MerchantAvatar] falls back to a colored
 * circle showing the merchant's initial, tinted with [avatarColor].
 */
data class Merchant(
    val id: String,
    val name: String,
    val avatarColor: Color,
    @DrawableRes val logoRes: Int? = null
)

/**
 * Dummy data standing in for a real "recent merchants" source (e.g. derived
 * from the user's past transactions) and a merchant directory. Kept
 * separate from the composables so swapping either list for a repository
 * call later doesn't require touching MerchantScreen.kt at all.
 */
val mockRecentMerchants = listOf(
    Merchant(id = "swiggy", name = "Swiggy", avatarColor = Color(0xFFFC8019)),
    Merchant(id = "amazon", name = "Amazon", avatarColor = Color(0xFF232F3E)),
    Merchant(id = "uber", name = "Uber", avatarColor = Color(0xFF000000)),
    Merchant(id = "zomato", name = "Zomato", avatarColor = Color(0xFFE23744))
)

val mockAllMerchants = listOf(
    Merchant(id = "blinkit", name = "Blinkit", avatarColor = Color(0xFFF8CB46)),
    Merchant(id = "myntra", name = "Myntra", avatarColor = Color(0xFFFF3F6C)),
    Merchant(id = "makemytrip", name = "MakeMyTrip", avatarColor = Color(0xFFE74C3C))
)