package com.example.novari.core.logging

import android.util.Log
import timber.log.Timber

/**
 * Release logging policy: drop VERBOSE/DEBUG/INFO (may contain SMS/transaction
 * data), forward only WARN/ERROR. Swap the Log.println call for a crash
 * reporter (e.g. Crashlytics) when one is wired up.
 */
class ReleaseTree : Timber.Tree() {
    override fun isLoggable(tag: String?, priority: Int): Boolean =
        priority >= Log.WARN

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (!isLoggable(tag, priority)) return
        Log.println(priority, tag ?: "Novari", message)
    }
}
