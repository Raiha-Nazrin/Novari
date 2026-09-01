package com.example.novari.navigation

//centralized navigation destination
sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object Dashboard : Screen("dashboard")
    data object SetupPermission : Screen("setup_permission")

    data object Appearance : Screen("AppearanceScreen")

    data object Contact : Screen("contact")

    data object TransactionList : Screen("transaction_list")


    data object NavigationDetail : Screen(
        "transaction_detail/{transactionId}"
    ) {
        const val ARG_TRANSACTION_ID = "transactionId"
        fun createRoute(transactionId: String) = "transaction_detail/$transactionId"
    }

    data object LegalDocument : Screen("legal_document/{docType}") {
        const val ARG_DOC_TYPE = "docType"
        fun createRoute(docType: LegalDocType) = "legal_document/${docType.name}"
    }
}

enum class LegalDocType(val assetPath: String, val title: String) {
    PRIVACY_POLICY("legal/privacy-policy.html", "Privacy Policy"),
    TERMS("legal/terms.html", "Terms")
}