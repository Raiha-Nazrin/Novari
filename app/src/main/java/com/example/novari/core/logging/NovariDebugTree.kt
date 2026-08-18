package com.example.novari.core.logging

import timber.log.Timber

private const val TAG_PREFIX = "Novari/"

class NovariDebugTree : Timber.DebugTree() {
    override fun createStackElementTag(element: StackTraceElement): String =
        TAG_PREFIX + super.createStackElementTag(element)
}
