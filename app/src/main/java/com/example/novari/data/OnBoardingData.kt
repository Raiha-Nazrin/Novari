package com.example.novari.data

import com.example.novari.R
import com.example.novari.ui.screens.onboarding.OnboardingPage

val onboardingPages: List<OnboardingPage> = listOf(
    OnboardingPage(
        titleRes = R.string.onboarding_title_1,
        introRes = R.string.onboarding_intro_1,
        bodyRes = R.string.onboarding_body_1,
        accentRes = R.string.onboarding_accent_1,
        imageRes = R.drawable.img_onboarding
    ),
    OnboardingPage(
        titleRes = R.string.onboarding_title_2,
        introRes = R.string.onboarding_intro_2,
        bodyRes = R.string.onboarding_body_2,
        accentRes = R.string.onboarding_accent_2,
        imageRes = R.drawable.img_onboarding2
    ),
    OnboardingPage(
        titleRes = R.string.onboarding_title_3,
        introRes = R.string.onboarding_intro_3,
        bodyRes = R.string.onboarding_body_3,
        accentRes = R.string.onboarding_accent_3,
        imageRes = R.drawable.img_onboarding3
    )
)
