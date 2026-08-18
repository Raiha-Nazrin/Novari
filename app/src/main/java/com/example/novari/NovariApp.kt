package com.example.novari

import android.app.Application
import com.example.novari.core.logging.NovariDebugTree
import com.example.novari.core.logging.ReleaseTree
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class NovariApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(if (BuildConfig.DEBUG) NovariDebugTree() else ReleaseTree())
    }
}
