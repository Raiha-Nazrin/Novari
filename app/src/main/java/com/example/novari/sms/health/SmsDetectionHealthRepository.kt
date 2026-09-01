package com.example.novari.sms.health

import kotlinx.coroutines.flow.Flow

interface SmsDetectionHealthRepository {
    /** Live count of successfully booked SMS transactions -- detection health screen. */
    fun observeProcessedCount(): Flow<Int>

    /** Live count of messages dismissed as non-financial noise. */
    fun observeIgnoredCount(): Flow<Int>

    /** Last time the catch-up sweep completed successfully, or null if it has never run. */
    fun observeLastSuccessfulSweepAt(): Flow<Long?>
}
