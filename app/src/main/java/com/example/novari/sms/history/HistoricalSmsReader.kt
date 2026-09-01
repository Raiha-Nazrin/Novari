package com.example.novari.sms.history

import android.content.ContentResolver
import android.provider.Telephony
import com.example.novari.sms.model.RawSmsMessage
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

class HistoricalSmsReader(private val contentResolver: ContentResolver) {
    suspend fun process(startTimestamp: Long, endTimestamp: Long, onMessage: suspend (RawSmsMessage) -> Unit) {
        val projection = arrayOf(Telephony.Sms.ADDRESS, Telephony.Sms.DATE, Telephony.Sms.BODY)
        val selection = "${Telephony.Sms.DATE} >= ? AND ${Telephony.Sms.DATE} <= ?"
        val args = arrayOf(startTimestamp.toString(), endTimestamp.toString())

        contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            projection,
            selection,
            args,
            "${Telephony.Sms.DATE} ASC"
        )?.use { cursor ->
            val address = cursor.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)
            val date = cursor.getColumnIndexOrThrow(Telephony.Sms.DATE)
            val body = cursor.getColumnIndexOrThrow(Telephony.Sms.BODY)
            while (cursor.moveToNext()) {
                currentCoroutineContext().ensureActive()
                val smsBody = cursor.getString(body) ?: continue
                onMessage(RawSmsMessage(cursor.getString(address), smsBody, cursor.getLong(date)))
            }
        }
    }
}
