package com.example.novari.data

import com.example.novari.R
import com.example.novari.ui.screens.onboarding.OnboardingPage

val onboardingPages: List<OnboardingPage> = listOf(
    OnboardingPage(
        title = "Welcome",
        description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
                "Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
        imageRes = R.drawable.img_onboarding
    ),
    OnboardingPage(
        title = "Stay Organized",
        description = "Ut enim ad minim veniam, quis nostrud exercitation ullamco " +
                "laboris nisi ut aliquip ex ea commodo consequat.",
        imageRes = R.drawable.img_onboarding2
    ),
    OnboardingPage(
        title = "Get Started",
        description = "Duis aute irure dolor in reprehenderit in voluptate velit " +
                "esse cillum dolore eu fugiat nulla pariatur.",
        imageRes = R.drawable.img_onboarding3
    )
)