package com.example.novari.sms.security

import java.security.MessageDigest
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object SmsFingerprint {
    private val dayFormatter = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneId.systemDefault())

    /**
     * Content-derived identity so the real-time (intent) and catch-up (content-provider) paths
     * converge on the same fingerprint despite differing timestamps (SMSC vs. framework receive
     * time) and sender formatting (+91 prefix, dashes, spaces).
     */
    fun create(sender: String?, body: String, timestamp: Long): String {
        val canonical = "${normalizeSender(sender)}|${normalizeBody(body)}|${dayBucket(timestamp)}"
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(canonical.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    private fun normalizeSender(sender: String?): String =
        sender.orEmpty()
            .uppercase()
            .replace("+91", "")
            .filter { it.isLetterOrDigit() }

    private fun normalizeBody(body: String): String =
        body.lowercase()
            .replace(Regex("\\s+"), " ")
            .trim()

    private fun dayBucket(timestamp: Long): String =
        dayFormatter.format(Instant.ofEpochMilli(timestamp))
}
