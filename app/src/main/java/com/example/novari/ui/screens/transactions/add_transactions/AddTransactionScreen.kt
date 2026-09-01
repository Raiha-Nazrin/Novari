package com.example.novari.ui.screens.transactions.add_transactions
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.novari.ui.theme.NovariColors
@Composable
fun AddTransactionScreen(
    viewModel: AddExpenseViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AddTransactionContent(
        uiState = uiState,
        onAmountChanged = viewModel::onAmountChanged,
        onMerchantChanged = viewModel::onMerchantChanged,
        onNotesChanged = viewModel::onNotesChanged,
        onSave = {
            viewModel.saveExpense(
                onSuccess = onNavigateBack
            )
        },
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTransactionContent(
    uiState: AddExpenseUiState,
    onAmountChanged: (String) -> Unit,
    onMerchantChanged: (String) -> Unit,
    onNotesChanged: (String) -> Unit,
    onSave: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    Scaffold(
        containerColor = NovariColors.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Add Expense", style = MaterialTheme.typography.titleMedium)
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        enabled = !uiState.isSaving
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = NovariColors.Navy
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = NovariColors.Background,
                    titleContentColor = NovariColors.Navy,
                    navigationIconContentColor = NovariColors.Navy
                )
            )
        },
        bottomBar = {
            Surface(
                color = NovariColors.Surface,
                tonalElevation = 3.dp
            ) {
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        onSave()
                    },
                    enabled = !uiState.isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .navigationBarsPadding()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Save Expense")
                    }
                }
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = "Transaction details",
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = "Add the details of your expense.",
                style = MaterialTheme.typography.bodyMedium,
                color = NovariColors.Slate
            )

            Spacer(modifier = Modifier.height(4.dp))

            AmountField(
                value = uiState.amountText,
                onValueChange = onAmountChanged,
                isError = uiState.errorMessage != null
            )

            OutlinedTextField(
                value = uiState.merchant,
                onValueChange = onMerchantChanged,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text("Merchant")
                },
                placeholder = {
                    Text("e.g. Lulu Hypermarket")
                },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Store,
                        contentDescription = null
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                )
            )

            OutlinedTextField(
                value = uiState.notes,
                onValueChange = onNotesChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                label = {
                    Text("Notes")
                },
                placeholder = {
                    Text("Add a note (optional)")
                },
                minLines = 4,
                maxLines = 6
            )

            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = NovariColors.Error
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun AmountField(
    value: String,
    onValueChange: (String) -> Unit,
    isError: Boolean
) {
    OutlinedTextField(
        value = value,
        onValueChange = { input ->
            val filtered = input
                .filter { it.isDigit() || it == '.' }
                .let { text ->
                    val dotIndex = text.indexOf('.')

                    if (dotIndex >= 0) {
                        val integerPart = text.substring(0, dotIndex)
                        val decimalPart = text
                            .substring(dotIndex + 1)
                            .take(2)

                        "$integerPart.$decimalPart"
                    } else {
                        text
                    }
                }

            onValueChange(filtered)
        },
        modifier = Modifier.fillMaxWidth(),
        label = {
            Text("Amount")
        },
        placeholder = {
            Text("0.00")
        },
        prefix = {
            Text(
                text = "₹ ",
                style = MaterialTheme.typography.titleMedium
            )
        },
        singleLine = true,
        isError = isError,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Next
        )
    )
}