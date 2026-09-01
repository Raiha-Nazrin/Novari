package com.example.novari.sms.security

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class SmsFingerprintTest {

    private fun localMillis(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long =
        ZonedDateTime.of(year, month, day, hour, minute, 0, 0, ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

    @Test
    fun `same message via intent and content-provider paths produces the same fingerprint`() {
        // Real-time path: SMSC timestamp, sender as delivered by the carrier.
        val realtimeFingerprint = SmsFingerprint.create(
            sender = "+91HDFCBK",
            body = "Rs 450.00 debited from A/c XX1234 to SWIGGY",
            timestamp = localMillis(2026, 8, 18, 10, 15)
        )

        // Catch-up path: framework receive time (differs from SMSC time), sender normalized
        // differently by the content provider (no +91, dashes).
        val catchUpFingerprint = SmsFingerprint.create(
            sender = "HDFC-BK",
            body = "  rs 450.00   debited from a/c xx1234 to swiggy  ",
            timestamp = localMillis(2026, 8, 18, 10, 17)
        )

        assertEquals(realtimeFingerprint, catchUpFingerprint)
    }

    @Test
    fun `same message on a later day produces a different fingerprint`() {
        val today = SmsFingerprint.create(
            sender = "HDFCBK",
            body = "Rs 450.00 debited from A/c XX1234 to SWIGGY",
            timestamp = localMillis(2026, 8, 18, 10, 15)
        )
        val nextDay = SmsFingerprint.create(
            sender = "HDFCBK",
            body = "Rs 450.00 debited from A/c XX1234 to SWIGGY",
            timestamp = localMillis(2026, 8, 19, 10, 15)
        )

        assertNotEquals(today, nextDay)
    }

    @Test
    fun `different body produces a different fingerprint`() {
        val a = SmsFingerprint.create("HDFCBK", "Rs 450.00 debited to SWIGGY", localMillis(2026, 8, 18, 10, 15))
        val b = SmsFingerprint.create("HDFCBK", "Rs 451.00 debited to SWIGGY", localMillis(2026, 8, 18, 10, 15))

        assertNotEquals(a, b)
    }

    @Test
    fun `dedup candidates include the own-day fingerprint and both neighbors`() {
        val sender = "HDFCBK"
        val body = "Rs 450.00 debited from A/c XX1234 to SWIGGY"
        val timestamp = localMillis(2026, 8, 18, 23, 59)

        val candidates = SmsFingerprint.dedupCandidates(sender, body, timestamp)

        assertEquals(3, candidates.size)
        assertTrue(candidates.contains(SmsFingerprint.create(sender, body, timestamp)))
        assertTrue(candidates.contains(SmsFingerprint.create(sender, body, localMillis(2026, 8, 17, 12, 0))))
        assertTrue(candidates.contains(SmsFingerprint.create(sender, body, localMillis(2026, 8, 19, 12, 0))))
    }

    @Test
    fun `messages just across a midnight boundary share a dedup candidate`() {
        val sender = "HDFCBK"
        val body = "Rs 450.00 debited from A/c XX1234 to SWIGGY"

        // Real-time path fingerprints the message just before local midnight.
        val beforeMidnight = SmsFingerprint.create(sender, body, localMillis(2026, 8, 18, 23, 59))
        // Catch-up path re-derives it a moment later, now on the other side of midnight.
        val afterMidnightCandidates = SmsFingerprint.dedupCandidates(sender, body, localMillis(2026, 8, 19, 0, 1))

        assertTrue(afterMidnightCandidates.contains(beforeMidnight))
    }
}
