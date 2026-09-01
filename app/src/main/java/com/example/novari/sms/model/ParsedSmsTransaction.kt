package com.example.novari.sms.model

import com.example.novari.core.model.TransactionType

data class ParsedSmsTransaction(
    val amountMinor: Long,
    val currencyCode: String,
    val merchant: String?,
    val transactionType: TransactionType,
    val transactionDate: Long,
    /** Bank/UPI reference number extracted from the message, when present. */
    val referenceNumber: String?
)
