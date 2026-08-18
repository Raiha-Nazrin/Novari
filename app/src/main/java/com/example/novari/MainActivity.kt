package com.example.novari

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.novari.navigation.AppNavigation
import com.example.novari.sms.permission.SmsPermissionState
import com.example.novari.sms.receiver.SmsSyncScheduler
import com.example.novari.ui.theme.NovariTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NovariTheme {
                AppNavigation()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (SmsPermissionState.canRead(this)) {
            SmsSyncScheduler.enqueue(this)
        }
    }
}