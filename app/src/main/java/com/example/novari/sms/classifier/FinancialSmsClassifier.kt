package com.example.novari.sms.classifier

class FinancialSmsClassifier {
    // Transaction verbs only. "bank"/"account" alone match promos and balance alerts too.
    private val transactionVerb = Regex(
        """\b(debited|credited|debit|credit|paid|spent|received)\b""",
        RegexOption.IGNORE_CASE
    )
    private val otpSignals = listOf("otp", "verification code", "one time password")

    fun isFinancial(sender: String?, body: String): Boolean {
        val text = body.lowercase()
        if (otpSignals.any(text::contains)) return false
        return transactionVerb.containsMatchIn(text)
    }
}
