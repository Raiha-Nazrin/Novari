package com.example.novari.sms.classifier

class FinancialSmsClassifier {
    // Transaction verbs only. "bank"/"account" alone match promos and balance alerts too.
    // Negative lookahead excludes "credit/debit card(s)" noun phrases (e.g. "Upto 5%
    // cashback-SBI Credit Cards") which otherwise false-match the bare "credit"/"debit" verb.
    private val transactionVerb = Regex(
        """\b(debited|credited|debit|credit|paid|spent|received)\b(?!\s*cards?\b)""",
        RegexOption.IGNORE_CASE
    )
    private val otpSignals = listOf("otp", "verification code", "one time password")

    // Marketing phrasing that legitimate debit/credit alerts don't use, even though
    // promos often borrow "credited"/"cashback" wording that would otherwise pass the
    // transaction-verb check (e.g. "Rs 500 cashback credited instantly, use code SAVE500").
    private val promotionalSignals = listOf(
        Regex("""\d+%\s*off"""),
        Regex("""\bflat\s+(?:rs\.?|inr|₹)?\s*[0-9,]+\s+off\b"""),
        Regex("""\b(?:use|promo|coupon)\s*code\b"""),
        Regex("""\blimited\s+period\s+offer\b"""),
        Regex("""\bt(?:&|n)?c\s*apply\b"""),
        Regex("""\bclick\s+here\b"""),
        Regex("""\bshop\s+now\b"""),
        Regex("""\bhurry\b"""),
        Regex("""\blucky\s+draw\b"""),
        Regex("""\bwin\s+(?:exciting\s+)?(?:prizes|rewards|gifts)\b"""),
        Regex("""\bunsubscribe\b"""),
        Regex("""\bavail\s+(?:now|this|the)\b"""),
        Regex("""\bpre-?approved\b"""),
        Regex("""\bon\s+your\s+next\s+(?:purchase|order|transaction|booking)\b""")
    )

    fun isFinancial(body: String): Boolean {
        val text = body.lowercase()
        if (otpSignals.any(text::contains)) return false
        if (promotionalSignals.any { it.containsMatchIn(text) }) return false
        return transactionVerb.containsMatchIn(text)
    }
}
