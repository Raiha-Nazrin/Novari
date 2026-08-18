package com.example.novari.sms.parser

import com.example.novari.core.model.TransactionType
import com.example.novari.sms.model.ParsedSmsTransaction
import com.example.novari.sms.model.RawSmsMessage
import java.math.BigDecimal
import java.math.RoundingMode

class FinancialSmsParser {
    // Anchored to a transaction verb first so account-number and balance digits
    // ("A/c XX1234 debited Rs 450, avl bal Rs 12,300") aren't picked up instead of the amount.
    private val amountPatterns = listOf(
        Regex("""(?:debited|credited|paid|spent|received|sent)\s+(?:by|with|for)?\s*(?:rs\.?|inr|₹)\s*([0-9][0-9,]*(?:\.[0-9]{1,2})?)""", RegexOption.IGNORE_CASE),
        Regex("""(?:rs\.?|inr|₹)\s*([0-9][0-9,]*(?:\.[0-9]{1,2})?)\s+(?:has been |is )?(?:debited|credited|paid|spent|received|sent)""", RegexOption.IGNORE_CASE),
        Regex("""(?:amount|amt)\s*(?:of|:)?\s*(?:rs\.?|inr|₹)?\s*([0-9][0-9,]*(?:\.[0-9]{1,2})?)""", RegexOption.IGNORE_CASE),
        Regex("""(?:rs\.?|inr|₹)\s*([0-9][0-9,]*(?:\.[0-9]{1,2})?)""", RegexOption.IGNORE_CASE)
    )

    private val merchantPatterns = listOf(
        Regex("""\b(?:to|at|from|merchant)\s+([A-Za-z0-9&.'_-]{2,40})""", RegexOption.IGNORE_CASE),
        Regex("""\bVPA\s*[:\-]?\s*([A-Za-z0-9@._-]{3,80})""", RegexOption.IGNORE_CASE)
    )

    private val referencePatterns = listOf(
        Regex("""(?:ref(?:erence)?\.?\s*no\.?|ref\s*id)\s*[:\-]?\s*([A-Za-z0-9]{4,30})""", RegexOption.IGNORE_CASE),
        Regex("""(?:txn\s*id|transaction\s*id)\s*[:\-]?\s*([A-Za-z0-9]{4,30})""", RegexOption.IGNORE_CASE),
        Regex("""\bUPI\s*Ref(?:erence)?(?:\s*No)?\.?\s*[:\-]?\s*([A-Za-z0-9]{4,30})""", RegexOption.IGNORE_CASE)
    )

    fun parse(message: RawSmsMessage): ParsedSmsTransaction? {
        val body = message.body.replace(Regex("\\s+"), " ").trim()
        val amount = amountPatterns.asSequence()
            .mapNotNull { it.find(body)?.groupValues?.getOrNull(1) }
            .mapNotNull(::toMinorUnits)
            .firstOrNull() ?: return null

        val type = when {
            listOf("debited", "debit", "paid", "spent", "sent").any(body.lowercase()::contains) -> TransactionType.EXPENSE
            listOf("credited", "credit", "received").any(body.lowercase()::contains) -> TransactionType.INCOME
            else -> return null
        }

        val merchant = merchantPatterns.asSequence()
            .mapNotNull { it.find(body)?.groupValues?.getOrNull(1) }
            .map { it.trim().trimEnd('.', ',', ';', ':') }
            .firstOrNull { it.isNotBlank() }

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

    private fun toMinorUnits(value: String): Long? = runCatching {
        BigDecimal(value.replace(",", ""))
            .setScale(2, RoundingMode.HALF_UP)
            .movePointRight(2)
            .longValueExact()
    }.getOrNull()?.takeIf { it > 0 }
}
