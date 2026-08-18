package com.example.novari.sms.parser

import com.example.novari.core.model.TransactionType
import com.example.novari.sms.model.RawSmsMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class FinancialSmsParserTest {

    private val parser = FinancialSmsParser()

    private fun message(body: String, timestamp: Long = 1_700_000_000_000L) =
        RawSmsMessage(sender = "HDFCBK", body = body, timestamp = timestamp)

    @Test
    fun `hdfc debit alert extracts amount and merchant`() {
        val parsed = parser.parse(
            message("Rs 450.00 debited from A/c XX1234 to SWIGGY on 01-01-24. UPI Ref No 123456789012.")
        )

        assertNotNull(parsed)
        assertEquals(45000L, parsed!!.amountMinor)
        assertEquals(TransactionType.EXPENSE, parsed.transactionType)
        assertEquals("SWIGGY", parsed.merchant)
        assertEquals("123456789012", parsed.referenceNumber)
    }

    @Test
    fun `icici credit alert with rupee symbol`() {
        val parsed = parser.parse(
            message("₹5,000.00 credited to your account from AMAZON. Txn ID: TXN99887766.")
        )

        assertNotNull(parsed)
        assertEquals(500000L, parsed!!.amountMinor)
        assertEquals(TransactionType.INCOME, parsed.transactionType)
    }

    @Test
    fun `sbi upi payment with amount containing comma`() {
        val parsed = parser.parse(
            message("You have paid Rs.12,345.67 to MERCHANT STORE via UPI. Ref No 555444333.")
        )

        assertNotNull(parsed)
        assertEquals(1234567L, parsed!!.amountMinor)
        assertEquals(TransactionType.EXPENSE, parsed.transactionType)
    }

    @Test
    fun `account and balance digits are not mistaken for the amount`() {
        val parsed = parser.parse(
            message("A/c XX1234 debited Rs 450, avl bal Rs 12,300")
        )

        assertNotNull(parsed)
        assertEquals(45000L, parsed!!.amountMinor)
    }

    @Test
    fun `multipart joined body still yields the amount`() {
        val part1 = "Rs 2,500.00 has been debited from your account "
        val part2 = "ending 1234 towards NETFLIX SUBSCRIPTION. Avl Bal Rs 45,000."
        val parsed = parser.parse(message(part1 + part2))

        assertNotNull(parsed)
        assertEquals(250000L, parsed!!.amountMinor)
        assertEquals(TransactionType.EXPENSE, parsed.transactionType)
    }

    @Test
    fun `balance alert without a transaction verb is not a transaction`() {
        val parsed = parser.parse(
            message("Your available balance as of today is Rs 45,000.00")
        )

        assertNull(parsed)
    }

    @Test
    fun `otp message has no debit or credit verb so it does not parse`() {
        val parsed = parser.parse(
            message("123456 is your OTP for a transaction of Rs 500. Do not share it with anyone.")
        )

        assertNull(parsed)
    }
}
