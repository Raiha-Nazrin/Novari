package com.example.novari.sms.parser

import com.example.novari.core.model.TransactionType
import com.example.novari.sms.model.ParsedSmsTransaction
import com.example.novari.sms.model.RawSmsMessage
import java.math.BigDecimal
import java.math.RoundingMode

class FinancialSmsParser {
    // Anchored to a transaction verb first so account-number and balance digits
    // ("A/c XX1234 debited Rs 450, avl bal Rs 12,300") aren't picked up instead of the amount.
    // Currency token is optional on the verb-led pattern -- SBI's UPI debit alerts ("debited
    // by 30.00 on date ...") never include Rs/INR/₹ at all. The connector word stays mandatory
    // and a trailing (?![A-Za-z0-9]) guard keeps the capture off dates/account tails/phone
    // numbers that happen to follow a verb without an amount ever being present.
    private val amountPatterns = listOf(
        Regex(
            """(?:debited|credited|paid|spent|received|sent|withdrawn|deducted)\s+(?:by|with|for|of)\s*(?:rs\.?|inr|₹)?\s*([0-9][0-9,]*(?:\.[0-9]{1,2})?)(?:/-)?(?![A-Za-z0-9])""",
            RegexOption.IGNORE_CASE
        ),
        Regex("""(?:rs\.?|inr|₹)\s*([0-9][0-9,]*(?:\.[0-9]{1,2})?)(?:/-)?\s+(?:has been |is )?(?:debited|credited|paid|spent|received|sent)""", RegexOption.IGNORE_CASE),
        Regex("""(?:amount|amt)\s*(?:of|:)?\s*(?:rs\.?|inr|₹)?\s*([0-9][0-9,]*(?:\.[0-9]{1,2})?)(?:/-)?""", RegexOption.IGNORE_CASE),
        Regex("""(?:rs\.?|inr|₹)\s*([0-9][0-9,]*(?:\.[0-9]{1,2})?)(?:/-)?""", RegexOption.IGNORE_CASE)
    )

    // Most-specific direction first ("trf to"/"transfer from") so the counterparty name wins
    // over an earlier, incidental "to|at|from" (e.g. "debited from A/c XX1234 to SWIGGY" would
    // otherwise anchor on "from A/c..." before reaching the real merchant).
    private val merchantPatterns = listOf(
        Regex("""\b(?:trf|transfer|sent|paid)\s+to\s+([A-Za-z0-9&.'_-][A-Za-z0-9&.'_ -]{1,80})""", RegexOption.IGNORE_CASE),
        Regex("""\b(?:received|credited|transfer)\s+from\s+([A-Za-z0-9&.'_-][A-Za-z0-9&.'_ -]{1,80})""", RegexOption.IGNORE_CASE),
        Regex("""\bVPA\s*[:\-]?\s*([A-Za-z0-9@._-]{3,80})""", RegexOption.IGNORE_CASE),
        Regex("""\b(?:to|at|from|merchant)\s+([A-Za-z0-9&.'_-][A-Za-z0-9&.'_ -]{1,80})""", RegexOption.IGNORE_CASE)
    )

    // Terminates a greedily-captured merchant run at the point it drifts into surrounding
    // sentence noise (ref numbers, dates, boilerplate) rather than the counterparty name.
    private val merchantStopWords = setOf(
        "on", "ref", "refno", "rrn", "utr", "upi", "txn", "avl", "bal", "via", "id",
        "dated", "date", "if", "for", "your", "the", "info", "call", "account", "acct",
        "and", "please", "not", "you", "no", "other", "services"
    )

    private val referencePatterns = listOf(
        Regex("""(?:ref(?:erence)?\.?\s*no\.?|ref\s*id)\s*[:\-]?\s*([A-Za-z0-9]{4,30})""", RegexOption.IGNORE_CASE),
        Regex("""(?:txn\s*id|transaction\s*id)\s*[:\-]?\s*([A-Za-z0-9]{4,30})""", RegexOption.IGNORE_CASE),
        Regex("""\bUPI\s*Ref(?:erence)?(?:\s*No)?\.?\s*[:\-]?\s*([A-Za-z0-9]{4,30})""", RegexOption.IGNORE_CASE),
        Regex("""\b(?:RRN|UTR)\s*[:\-]?\s*([A-Za-z0-9]{4,30})""", RegexOption.IGNORE_CASE)
    )

    // Word-boundary so "prepaid"/"repaid"/"consent" don't false-match "paid"/"sent" as substrings.
    private val expenseVerb = Regex("""\b(?:debited|debit|paid|spent|withdrawn|deducted|sent)\b""", RegexOption.IGNORE_CASE)
    private val incomeVerb = Regex("""\b(?:credited|credit|received|deposited)\b""", RegexOption.IGNORE_CASE)

    // Billers/lenders describe the user's own payment from their side, e.g. "payment of
    // Rs 3600 towards your loan account ... has been received" -- that's the user paying,
    // even though "received" alone would read as income. Only overrides when nothing in
    // the message explicitly credits the user's own account/wallet/card.
    private val billerPaymentReceived = Regex("""\bpayment\b(?:.{0,120}?)\breceived\b""", RegexOption.IGNORE_CASE)
    private val creditedToUser = Regex("""\b(?:credited|deposited)\s+(?:to|in)\s+(?:your|the)\s*(?:a/?c|account|wallet|card)""", RegexOption.IGNORE_CASE)

    // A reversal/refund of an earlier debit is money coming back -- income -- even though the
    // message itself often still contains the word "debit" (e.g. "your debit of Rs 500 ...
    // has been reversed"), which would otherwise win the expense/income position race below.
    private val reversalOrRefund = Regex("""\b(?:reversed|reversal|refunded|refund)\b""", RegexOption.IGNORE_CASE)

    // Messages that mention money without describing a settled transaction on the user's
    // account: future-dated mandate debits, failed/declined attempts, incoming payment
    // requests, and due-date/limit reminders. Checked before amount/type extraction so none
    // of them are ever booked as a transaction.
    private val nonTransactionSignals = listOf(
        Regex("""\bwill\s+be\s+(?:debited|deducted|charged)\b""", RegexOption.IGNORE_CASE),
        Regex("""\bscheduled\s+(?:for|on)\b""", RegexOption.IGNORE_CASE),
        Regex("""\b(?:failed|declined|unsuccessful|not\s+processed|cancelled)\b""", RegexOption.IGNORE_CASE),
        Regex("""\b(?:has\s+)?requested\s+(?:rs\.?|inr|₹)?\s*[0-9]""", RegexOption.IGNORE_CASE),
        Regex("""\bcollect\s+request\b""", RegexOption.IGNORE_CASE),
        Regex("""\bpayment\s+request\b""", RegexOption.IGNORE_CASE),
        Regex("""\b(?:is|are)\s+due\s+on\b""", RegexOption.IGNORE_CASE),
        Regex("""\bdue\s+on\b""", RegexOption.IGNORE_CASE),
        Regex("""\bmin(?:imum)?\.?\s*amount\s+due\b""", RegexOption.IGNORE_CASE),
        Regex("""\bcredit\s+limit\b""", RegexOption.IGNORE_CASE),
        Regex("""\bpre-?approved\b""", RegexOption.IGNORE_CASE),
        Regex("""\beligible\s+for\b""", RegexOption.IGNORE_CASE)
    )

    // Mandate/autopay/e-NACH chatter (created, revoked, active, upcoming debit) is not a
    // transaction unless it's reporting a debit that has already settled.
    private val mandateOrAutopay = Regex("""\b(?:mandate|e-?nach|autopay|standing\s+instruction)\b""", RegexOption.IGNORE_CASE)
    private val settledPastTense = Regex("""\b(?:has\s+been|was)\s+(?:debited|credited|deducted|charged)\b""", RegexOption.IGNORE_CASE)

    fun parse(message: RawSmsMessage): ParsedSmsTransaction? {
        val body = message.body.replace(Regex("\\s+"), " ").trim()
        if (isNonTransactional(body)) return null

        val amount = amountPatterns.asSequence()
            .mapNotNull { it.find(body)?.groupValues?.getOrNull(1) }
            .mapNotNull(::toMinorUnits)
            .firstOrNull() ?: return null

        val type = detectType(body) ?: return null

        val merchant = merchantPatterns.asSequence()
            .mapNotNull { it.find(body)?.groupValues?.getOrNull(1) }
            .mapNotNull(::cleanMerchant)
            .firstOrNull()

        val referenceNumber = referencePatterns.asSequence()
            .mapNotNull { it.find(body)?.groupValues?.getOrNull(1) }
            .firstOrNull { it.isNotBlank() }

        return ParsedSmsTransaction(
            amountMinor = amount,
            currencyCode = "INR",
            merchant = merchant,
            transactionType = type,
            transactionDate = message.timestamp,
            referenceNumber = referenceNumber
        )
    }

    private fun isNonTransactional(body: String): Boolean {
        if (nonTransactionSignals.any { it.containsMatchIn(body) }) return true
        if (mandateOrAutopay.containsMatchIn(body) && !settledPastTense.containsMatchIn(body)) return true
        return false
    }

    // Trims a greedily-captured merchant run down to the counterparty name: walk word by
    // word and stop at the first sentence-noise token, long digit run (ref/phone number), or
    // the fifth word, whichever comes first.
    private fun cleanMerchant(raw: String): String? {
        val kept = mutableListOf<String>()
        for (word in raw.trim().split(Regex("\\s+"))) {
            val bare = word.trimEnd('.', ',', ';', ':')
            if (bare.isBlank()) continue
            if (bare.lowercase() in merchantStopWords) break
            if (bare.count(Char::isDigit) >= 6) break
            kept += bare
            if (kept.size >= 5) break
        }
        return kept.joinToString(" ").trim().trimEnd('.', ',', ';', ':').ifBlank { null }
    }

    // Picks whichever verb appears first in the body when both directions are mentioned
    // (e.g. an IMPS alert naming the sender's account as well as the receiver's), since
    // the earlier clause is the one describing what happened to the user's own account.
    private fun detectType(body: String): TransactionType? {
        if (reversalOrRefund.containsMatchIn(body)) {
            return TransactionType.INCOME
        }

        if (billerPaymentReceived.containsMatchIn(body) && !creditedToUser.containsMatchIn(body)) {
            return TransactionType.EXPENSE
        }

        val expenseMatch = expenseVerb.find(body)
        val incomeMatch = incomeVerb.find(body)
        return when {
            expenseMatch != null && incomeMatch != null ->
                if (expenseMatch.range.first <= incomeMatch.range.first) TransactionType.EXPENSE else TransactionType.INCOME
            expenseMatch != null -> TransactionType.EXPENSE
            incomeMatch != null -> TransactionType.INCOME
            else -> null
        }
    }

    private fun toMinorUnits(value: String): Long? = runCatching {
        BigDecimal(value.replace(",", ""))
            .setScale(2, RoundingMode.HALF_UP)
            .movePointRight(2)
            .longValueExact()
    }.getOrNull()?.takeIf { it > 0 }
}
