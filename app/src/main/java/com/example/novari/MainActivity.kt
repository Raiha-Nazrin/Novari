package com.example.novari

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.novari.navigation.AppNavigation
import com.example.novari.sms.permission.SmsPermissionState
import com.example.novari.sms.receiver.SmsSyncScheduler
import com.example.novari.ui.theme.NovariTheme
import com.example.novari.ui.theme.ThemeMode
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settings by viewModel.settings.collectAsStateWithLifecycle()

            settings?.let { appearanceSettings ->
                LaunchedEffect(appearanceSettings.theme) {
                    val isDark = appearanceSettings.theme == ThemeMode.DARK
                    enableEdgeToEdge(
                        statusBarStyle = if (isDark) {
                            SystemBarStyle.dark(AndroidColor.TRANSPARENT)
                        } else {
                            SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
                        },
                        navigationBarStyle = if (isDark) {
                            SystemBarStyle.dark(AndroidColor.TRANSPARENT)
                        } else {
                            SystemBarStyle.light(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
                        }
                    )
                }

                NovariTheme(settings = appearanceSettings) {
                    AppNavigation()
                }
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
