package com.example.novari.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.novari.R
import com.example.novari.ui.screens.search.AmountRange
import com.example.novari.ui.theme.NovariColors

/** Min/max rupee inputs for the Amount "Search by" scope, mirroring CategoryBottomSheetContent's layout. */
@Composable
fun AmountRangeBottomSheet(
    initialRange: AmountRange,
    onApply: (AmountRange) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    var minText by remember { mutableStateOf(initialRange.minMinor?.let { (it / 100).toString() } ?: "") }
    var maxText by remember { mutableStateOf(initialRange.maxMinor?.let { (it / 100).toString() } ?: "") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.amount_range),
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.weight(1f))

            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = NovariColors.Navy
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = minText,
                onValueChange = { minText = it.filter { ch -> ch.isDigit() } },
                modifier = Modifier.weight(1f),
                label = { Text("Min ₹") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            OutlinedTextField(
                value = maxText,
                onValueChange = { maxText = it.filter { ch -> ch.isDigit() } },
                modifier = Modifier.weight(1f),
                label = { Text("Max ₹") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TextButton(
                onClick = {
                    minText = ""
                    maxText = ""
                    onClear()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Clear")
            }

            Button(
                onClick = {
                    val min = minText.toLongOrNull()?.times(100)
                    val max = maxText.toLongOrNull()?.times(100)
                    onApply(AmountRange(minMinor = min, maxMinor = max))
                },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NovariColors.Teal,
                    contentColor = NovariColors.Surface
                )
            ) {
                Text("Apply")
            }
        }
    }
}
