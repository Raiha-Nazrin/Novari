package com.example.novari.ui.screens.transactions.transaction_detail

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.novari.ui.theme.NovariColors
import com.example.novari.ui.theme.NovariShape

@Composable
fun TransactionDetailsRoute(
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    viewModel: TransactionDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                TransactionDetailsEvent.Deleted -> onBackClick()
            }
        }
    }

    TransactionDetailsScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onEditClick = onEditClick,
        onDeleteClick = { showDeleteDialog = true }
    )

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = NovariColors.Surface,
            shape = NovariShape.card,
            title = { Text(text = "Delete transaction", style = MaterialTheme.typography.titleMedium) },
            text = { Text(text = "This transaction will be permanently removed. This can't be undone.", style = MaterialTheme.typography.bodyMedium) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    viewModel.deleteTransaction()
                }) {
                    Text(text = "Delete", color = NovariColors.Error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(text = "Cancel")
                }
            }
        )
    }
}
