package com.example.novari.sms.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class SmsBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // TODO: Extract minimal event data and hand off to a background-safe worker.
        // Never perform long-running parsing directly in onReceive().
    }
}
