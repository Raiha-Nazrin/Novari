package com.example.novari.ui.screens.transactions

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.novari.core.database.entity.TransactionEntity
import com.example.novari.core.model.TransactionSource
import com.example.novari.core.model.TransactionType
import com.example.novari.core.util.TransactionId
import com.example.novari.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

data class AddExpenseUiState(
    val amountText: String = "",
    val merchant: String = "",
    val notes: String = "",
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class AddExpenseViewModel @Inject constructor(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddExpenseUiState())
    val uiState: StateFlow<AddExpenseUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeActive()
                .catch { error ->
                    Log.e("ActiveTransactions", "Failed to observe transactions", error)
                }
                .collect { transactions ->
                }
        }
    }
    fun onAmountChanged(value: String) {
        _uiState.update { it.copy(amountText = value, errorMessage = null) }
    }

    fun onMerchantChanged(value: String) {
        _uiState.update { it.copy(merchant = value, errorMessage = null) }
    }

    fun onNotesChanged(value: String) {
        _uiState.update { it.copy(notes = value, errorMessage = null) }
    }

    fun saveExpense(onSuccess: () -> Unit) {
        val state = _uiState.value
        val amountMinor = state.amountText.toMinorUnitsOrNull()

        if (amountMinor == null || amountMinor <= 0L) {
            _uiState.update {
                it.copy(errorMessage = "Enter a valid amount")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            runCatching {
                val now = System.currentTimeMillis()

                repository.create(
                    TransactionEntity(
                        id = TransactionId.new(),
                        amountMinor = amountMinor,
                        currencyCode = "INR",
                        merchant = state.merchant.trim().ifBlank { null },
                        categoryId = null,
                        transactionType = TransactionType.EXPENSE,
                        transactionDate = now,
                        notes = state.notes.trim().ifBlank { null },
                        source = TransactionSource.MANUAL,
                        sourceReference = null,
                        createdAt = now,
                        updatedAt = now,
                        deletedAt = null,
                        revision = 1L
                    )
                )
            }.onSuccess {
                _uiState.value = AddExpenseUiState()
                onSuccess()
                runCatching {
                    repository.observeActive().collect { list ->

                    }
                }
            }.onFailure { e->
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "Unable to save expense"
                    )
                }

            }
        }
    }

    private fun String.toMinorUnitsOrNull(): Long? =
        runCatching {
            BigDecimal(trim())
                .setScale(2, RoundingMode.HALF_UP)
                .movePointRight(2)
                .longValueExact()
        }.getOrNull()
}
