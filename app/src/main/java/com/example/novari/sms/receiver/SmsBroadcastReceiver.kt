package com.example.novari.sms.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.example.novari.di.ApplicationScope
import com.example.novari.sms.model.RawSmsMessage
import com.example.novari.sms.permission.SmsPermissionState
import com.example.novari.sms.processor.SmsTransactionProcessor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SmsBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var smsTransactionProcessor: SmsTransactionProcessor

    @Inject
    @ApplicationScope
    lateinit var applicationScope: CoroutineScope

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return
        if (!SmsPermissionState.canReceive(context)) return

        // Multipart bank alerts split the amount and merchant across parts; parsing part 0
        // alone silently loses the amount. Group by originating address and join in order.
        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        val rawMessages = messages
            .groupBy { it.originatingAddress }
            .map { (sender, parts) ->
                RawSmsMessage(
                    sender = sender,
                    body = parts.joinToString(separator = "") { it.messageBody.orEmpty() },
                    timestamp = parts.maxOf { it.timestampMillis }
                )
            }

        // goAsync() keeps the body in memory only for the receiver's ~10s budget -- SMS bodies
        // must never be persisted (WorkManager Data is written to disk), so this cannot be
        // handed off to a Worker.
        val pendingResult = goAsync()
        applicationScope.launch {
            try {
                rawMessages.forEach { smsTransactionProcessor.process(it) }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
