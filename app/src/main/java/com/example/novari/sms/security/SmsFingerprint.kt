package com.example.novari.sms.security

import java.security.MessageDigest
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object SmsFingerprint {
    private val dayFormatter = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneId.systemDefault())
    private const val DAY_MILLIS = 24 * 60 * 60 * 1000L

    /**
     * Content-derived identity so the real-time (intent) and catch-up (content-provider) paths
     * converge on the same fingerprint despite differing timestamps (SMSC vs. framework receive
     * time) and sender formatting (+91 prefix, dashes, spaces).
     */
    fun create(sender: String?, body: String, timestamp: Long): String =
        forDay(sender, body, dayBucket(timestamp))

    /**
     * The stored fingerprint always buckets by the message's own day, but the real-time and
     * catch-up paths can disagree on which side of midnight a message falls (SMSC timestamp vs.
     * framework receive time). Dedup must therefore check the adjacent days' fingerprints too,
     * even though only [create]'s own-day value is ever persisted.
     */
    fun dedupCandidates(sender: String?, body: String, timestamp: Long): List<String> =
        listOf(-DAY_MILLIS, 0L, DAY_MILLIS).map { offset ->
            forDay(sender, body, dayBucket(timestamp + offset))
        }

    private fun forDay(sender: String?, body: String, day: String): String {
        val canonical = "${normalizeSender(sender)}|${normalizeBody(body)}|$day"
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
