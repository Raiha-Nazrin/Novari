package com.example.novari.ui.screens.transactions.transaction_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.novari.domain.repository.CategoryRepository
import com.example.novari.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

private const val TRANSACTION_ID_ARG = "transactionId"

sealed interface TransactionDetailsUiState {
    data object Loading : TransactionDetailsUiState
    data class Content(val transaction: TransactionDetailsUiModel) : TransactionDetailsUiState
    data object NotFound : TransactionDetailsUiState
}

sealed interface TransactionDetailsEvent {
    data object Deleted : TransactionDetailsEvent
}

@HiltViewModel
class TransactionDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val transactionRepository: TransactionRepository,
    categoryRepository: CategoryRepository
) : ViewModel() {

    private val transactionId: String = checkNotNull(savedStateHandle[TRANSACTION_ID_ARG])

    private val events = Channel<TransactionDetailsEvent>(Channel.BUFFERED)
    val eventFlow: Flow<TransactionDetailsEvent> = events.receiveAsFlow()

    val uiState: StateFlow<TransactionDetailsUiState> = combine(
        transactionRepository.observeById(transactionId),
        categoryRepository.observeActive()
    ) { transaction, categories ->
        if (transaction == null || transaction.deletedAt != null) {
            TransactionDetailsUiState.NotFound
        } else {
            val categoriesById = categories.associateBy { it.id }
            TransactionDetailsUiState.Content(transaction.toDetailsUiModel(categoriesById))
        }
    }.catch { error ->
        Timber.tag("TransactionDetailsViewModel").e(error, "Failed to load transaction $transactionId")
        emit(TransactionDetailsUiState.NotFound)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TransactionDetailsUiState.Loading
    )

    fun deleteTransaction() {
        viewModelScope.launch {
            val transaction = transactionRepository.findById(transactionId) ?: return@launch
            transactionRepository.delete(transaction)
            events.send(TransactionDetailsEvent.Deleted)
        }
    }
}
