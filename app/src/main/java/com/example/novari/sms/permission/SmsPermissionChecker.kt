package com.example.novari.sms.permission

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Injectable wrapper around [SmsPermissionState]'s static checks, so ViewModels that need
 * live permission state (health screen, degradation banner) don't have to depend on a raw
 * Android [Context] to stay unit-testable.
 */
interface SmsPermissionChecker {
    fun canRead(): Boolean
    fun canReceive(): Boolean
}

class AndroidSmsPermissionChecker @Inject constructor(
    @ApplicationContext private val context: Context
) : SmsPermissionChecker {
    override fun canRead(): Boolean = SmsPermissionState.canRead(context)
    override fun canReceive(): Boolean = SmsPermissionState.canReceive(context)
}
