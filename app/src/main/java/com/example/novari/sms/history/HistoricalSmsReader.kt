package com.example.novari.sms.history

import android.content.ContentResolver
import com.example.novari.sms.processor.RawSmsMessage
import javax.inject.Inject

class HistoricalSmsReader @Inject constructor(
    private val contentResolver: ContentResolver
) {
    suspend fun read(
        startTimestamp: Long,
        endTimestamp: Long
    ): List<RawSmsMessage> {
        // TODO: Query only the required SMS columns and date range.
        // Do not log or persist the message body.
        return emptyList()
    }
}
