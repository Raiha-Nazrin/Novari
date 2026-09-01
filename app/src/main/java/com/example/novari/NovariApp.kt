package com.example.novari

import android.app.Application
import com.example.novari.core.database.seed.CategorySeeder
import com.example.novari.core.database.seed.MerchantCategoryRuleSeeder
import com.example.novari.core.logging.NovariDebugTree
import com.example.novari.core.logging.ReleaseTree
import com.example.novari.di.ApplicationScope
import com.example.novari.sms.permission.SmsPermissionState
import com.example.novari.sms.receiver.SmsSyncScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import timber.log.Timber

@HiltAndroidApp
class NovariApp : Application() {

    @Inject
    lateinit var categorySeeder: CategorySeeder

    @Inject
    lateinit var merchantCategoryRuleSeeder: MerchantCategoryRuleSeeder

    @Inject
    @ApplicationScope
    lateinit var applicationScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        Timber.plant(if (BuildConfig.DEBUG) NovariDebugTree() else ReleaseTree())

        applicationScope.launch {
            categorySeeder.seedIfNeeded()
            merchantCategoryRuleSeeder.seedIfNeeded()
        }

        // Registered here, not just MainActivity.onStart(), so the sweep keeps running even if
        // the app is never foregrounded again after permission is granted.
        if (SmsPermissionState.canRead(this)) {
            SmsSyncScheduler.enqueuePeriodic(this)
        }
    }
}
