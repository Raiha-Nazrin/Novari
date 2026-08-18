package com.example.novari.sms.processor

/**
 * Runtime-only SMS representation.
 * Never persist or log the body.
 */
data class RawSmsMessage(
    val providerMessageId: String,
    val sender: String?,
    val body: String,
    val timestamp: Long
)
