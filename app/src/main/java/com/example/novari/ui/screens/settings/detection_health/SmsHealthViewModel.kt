package com.example.novari.ui.screens.settings.detection_health

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.novari.sms.health.SmsDetectionHealthRepository
import com.example.novari.sms.health.SmsDetectionHealthState
import com.example.novari.sms.permission.SmsPermissionChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class SmsHealthViewModel @Inject constructor(
    smsDetectionHealthRepository: SmsDetectionHealthRepository,
    private val smsPermissionChecker: SmsPermissionChecker
) : ViewModel() {

    private val smsPermissionState = MutableStateFlow(currentSmsPermissionSnapshot())

    val uiState: StateFlow<SmsDetectionHealthState> = combine(
        smsPermissionState,
        smsDetectionHealthRepository.observeLastSuccessfulSweepAt(),
        smsDetectionHealthRepository.observeProcessedCount(),
        smsDetectionHealthRepository.observeIgnoredCount()
    ) { permission, lastSweepAt, processed, ignored ->
        SmsDetectionHealthState(
            canReadSms = permission.first,
            canReceiveSms = permission.second,
            lastSuccessfulSweepAt = lastSweepAt,
            processedCount = processed,
            ignoredCount = ignored
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SmsDetectionHealthState()
    )

    fun refreshSmsPermissionState() {
        smsPermissionState.value = currentSmsPermissionSnapshot()
    }

    private fun currentSmsPermissionSnapshot(): Pair<Boolean, Boolean> =
        smsPermissionChecker.canRead() to smsPermissionChecker.canReceive()
}
