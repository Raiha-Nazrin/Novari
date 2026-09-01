package com.example.novari.sms.health

import java.util.concurrent.TimeUnit

/**
 * Everything the detection health screen and the Home degradation banner need, in one place,
 * so the "is this silently broken?" judgement is made exactly once and can't drift between
 * the two surfaces.
 */
data class SmsDetectionHealthState(
    val canReadSms: Boolean = false,
    val canReceiveSms: Boolean = false,
    val lastSuccessfulSweepAt: Long? = null,
    val processedCount: Int = 0,
    val ignoredCount: Int = 0
) {
    /** Whether SMS auto-tracking has ever been turned on -- otherwise there's nothing to degrade. */
    val isSetUp: Boolean get() = canReadSms || lastSuccessfulSweepAt != null

    val isSweepStale: Boolean
        get() {
            val last = lastSuccessfulSweepAt ?: return isSetUp
            return System.currentTimeMillis() - last > STALE_SWEEP_THRESHOLD_MILLIS
        }

    /** True once tracking was set up but silently stopped working -- see plan Phase 6.3. */
    val isDegraded: Boolean
        get() = isSetUp && (!canReadSms || !canReceiveSms || isSweepStale)

    companion object {
        val STALE_SWEEP_THRESHOLD_MILLIS: Long = TimeUnit.HOURS.toMillis(24)
    }
}
