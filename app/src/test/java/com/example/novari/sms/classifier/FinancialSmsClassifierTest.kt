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

    @Test
    fun `promo sender header is not financial even with a transaction verb`() {
        assertFalse(
            classifier.isFinancial(
                "AD-HDFCBK",
                "Rs 500 cashback credited instantly on your next transaction. Use code SAVE500."
            )
        )
    }

    @Test
    fun `cashback offer with promo language is not financial`() {
        assertFalse(
            classifier.isFinancial(
                "HDFCBK",
                "Get flat Rs 500 off + cashback credited instantly! Use code SAVE500. T&C apply."
            )
        )
    }

    @Test
    fun `discount percentage offer is not financial`() {
        assertFalse(
            classifier.isFinancial("BIGBSK", "Flat 20% off on your next order! Shop now, hurry!")
        )
    }

    @Test
    fun `real cashback credit without promo phrasing is still financial`() {
        assertTrue(
            classifier.isFinancial("HDFCBK", "Rs 100 cashback credited to your prepaid wallet")
        )
    }

    @Test
    fun `retailer sale blast mentioning credit cards is not financial`() {
        assertFalse(
            classifier.isFinancial(
                "VM-EASYBY-P",
                "Heading home for festivities? Get whole family celebration-ready@EasyBuy " +
                    "1000+ New Styles under Rs499 Fashion starts@199 Upto 5% cashback-SBI Credit Cards T&C"
            )
        )
    }

    @Test
    fun `flat price sale blast with no transaction verb is not financial`() {
        assertFalse(
            classifier.isFinancial(
                "VM-EASYBY-P",
                "LAST 5 DAYS! Flat Price Sale@EasyBuy-Kozhikode Everything under Rs199 on Sale " +
                    "Merchandise Ends 30 June Tees@99 Shirts@199 Kurtas@199 Kidswear@99 T&C"
            )
        )
    }

    @Test
    fun `debit card dispatch notice is not mistaken for a debit transaction`() {
        assertFalse(
            classifier.isFinancial("HDFCBK", "Your new Debit Card has been dispatched and will arrive in 5 days.")
        )
    }
}
