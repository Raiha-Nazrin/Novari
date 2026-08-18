package com.example.novari.sms.classifier

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FinancialSmsClassifierTest {

    private val classifier = FinancialSmsClassifier()

    @Test
    fun `debit alert is financial`() {
        assertTrue(classifier.isFinancial("HDFCBK", "Rs 450 debited from A/c XX1234 to SWIGGY"))
    }

    @Test
    fun `credit alert is financial`() {
        assertTrue(classifier.isFinancial("ICICIB", "Rs 5000 credited to your account"))
    }

    @Test
    fun `promo message mentioning bank and account is not financial`() {
        assertFalse(
            classifier.isFinancial(
                "HDFCBK",
                "Your HDFC Bank account is eligible for a pre-approved personal loan. Apply now!"
            )
        )
    }

    @Test
    fun `balance alert is not financial`() {
        assertFalse(classifier.isFinancial("SBIBNK", "Your account balance is Rs 45,000 as of today"))
    }

    @Test
    fun `otp message is not financial even if it mentions a transaction`() {
        assertFalse(
            classifier.isFinancial("HDFCBK", "123456 is your OTP for a transaction of Rs 500. Do not share.")
        )
    }

    @Test
    fun `verification code message is not financial`() {
        assertFalse(classifier.isFinancial("VM-ICICI", "Your verification code is 998877"))
    }
}
