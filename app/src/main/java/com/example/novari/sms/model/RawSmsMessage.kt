package com.example.novari.sms.model

data class RawSmsMessage(
    val sender: String?,
    val body: String,
    val timestamp: Long
)
