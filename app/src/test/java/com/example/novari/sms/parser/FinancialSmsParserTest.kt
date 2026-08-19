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

    @Test
    fun `biller confirming a loan payment received from the user is an expense`() {
        val parsed = parser.parse(
            message(
                "Dear Customer, payment of Rs. 3600 towards your loan Account 82.......1 has been " +
                    "received through BBPSPAYM on 2026-08-06 00:00:00.0 and updated in our records. " +
                    "You can pay dues through Gpay/PhonePe/PayTm/Bill Desk. For more details visit " +
                    "https://www.hdbfs.com/make-payment .Thank you. -HDBFS"
            )
        )

        assertNotNull(parsed)
        assertEquals(360000L, parsed!!.amountMinor)
        assertEquals(TransactionType.EXPENSE, parsed.transactionType)
    }

    @Test
    fun `credit alert mentioning debit card is not mistaken for an expense`() {
        val parsed = parser.parse(
            message("Rs 5,000 credited to A/c linked to your Debit Card ending 1234.")
        )

        assertNotNull(parsed)
        assertEquals(TransactionType.INCOME, parsed!!.transactionType)
    }

    @Test
    fun `prepaid wallet credit is not mistaken for a paid expense`() {
        val parsed = parser.parse(
            message("Rs 100 cashback credited to your prepaid wallet")
        )

        assertNotNull(parsed)
        assertEquals(TransactionType.INCOME, parsed!!.transactionType)
    }

    @Test
    fun `earlier verb wins when both directions are mentioned`() {
        val parsed = parser.parse(
            message("INR 5,000 credited to your A/c XX1 via NEFT, debited from sender A/c XX9 at ABC Bank.")
        )

        assertNotNull(parsed)
        assertEquals(TransactionType.INCOME, parsed!!.transactionType)
    }

    @Test
    fun `sbi upi debit alert with no currency token still extracts the amount`() {
        val parsed = parser.parse(
            message(
                "Dear UPI user A/C X2851 debited by 30.00 on date 03Aug26 trf to SHABEER V K " +
                    "Refno 621555777395 If not u? call-1800111109 for other services-18001234-SBI"
            )
        )

        assertNotNull(parsed)
        assertEquals(3000L, parsed!!.amountMinor)
        assertEquals(TransactionType.EXPENSE, parsed.transactionType)
        assertEquals("SHABEER V K", parsed.merchant)
        assertEquals("621555777395", parsed.referenceNumber)
    }

    @Test
    fun `sbi credit alert with hyphenated verb extracts multi-word merchant`() {
        val parsed = parser.parse(
            message(
                "Dear SBI User, your A/c X2851-credited by Rs.500 on 04Aug26 transfer from " +
                    "JACKSON PATRICK Ref No 658223165465 -SBI"
            )
        )

        assertNotNull(parsed)
        assertEquals(50000L, parsed!!.amountMinor)
        assertEquals(TransactionType.INCOME, parsed.transactionType)
        assertEquals("JACKSON PATRICK", parsed.merchant)
        assertEquals("658223165465", parsed.referenceNumber)
    }

    @Test
    fun `a reversed debit is income even though the message still says debit`() {
        val parsed = parser.parse(
            message("Your debit of Rs 500 to SWIGGY on 01-01-24 has been reversed. Ref No 123456789012.")
        )

        assertNotNull(parsed)
        assertEquals(50000L, parsed!!.amountMinor)
        assertEquals(TransactionType.INCOME, parsed.transactionType)
    }

    @Test
    fun `upi mandate revocation notice does not parse as a transaction`() {
        val parsed = parser.parse(
            message("UPI-Mandate successfully Revoked by JioHotstar for Rs149.00 -SBI")
        )

        assertNull(parsed)
    }

    @Test
    fun `future mandate debit does not parse as a transaction`() {
        val parsed = parser.parse(
            message("Mandate created for Rs 149.00. It will be debited on 05Sep26 -SBI")
        )

        assertNull(parsed)
    }

    @Test
    fun `settled mandate debit still parses as an expense`() {
        val parsed = parser.parse(
            message("Rs 149.00 has been debited from your A/c towards mandate NETFLIX on 05Sep26 -SBI")
        )

        assertNotNull(parsed)
        assertEquals(14900L, parsed!!.amountMinor)
        assertEquals(TransactionType.EXPENSE, parsed.transactionType)
    }

    @Test
    fun `declined transaction does not parse`() {
        val parsed = parser.parse(
            message("Your transaction of Rs 500 debited to MERCHANT has been declined. -SBI")
        )

        assertNull(parsed)
    }

    @Test
    fun `incoming collect request does not parse as a transaction`() {
        val parsed = parser.parse(
            message("Rahul has requested Rs 500 from you via UPI. Approve on your UPI app.")
        )

        assertNull(parsed)
    }

    @Test
    fun `emi due reminder does not parse as a transaction`() {
        val parsed = parser.parse(
            message("Your EMI of Rs 3,000 is due on 10Sep26. Please ensure sufficient balance.")
        )

        assertNull(parsed)
    }
}
