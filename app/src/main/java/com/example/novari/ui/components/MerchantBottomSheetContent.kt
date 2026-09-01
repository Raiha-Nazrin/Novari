package com.example.novari.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.novari.R
import com.example.novari.ui.model.MerchantUiModel
import com.example.novari.ui.theme.NovariColors

@Composable
fun MerchantBottomSheetContent(
    merchants: List<MerchantUiModel>,
    selectedMerchantKeys: Set<String>,
    onSelectionChanged: (Set<String>) -> Unit,
    onApply: () -> Unit,
    onDismiss: () -> Unit
) {
    val isAllSelected = selectedMerchantKeys.isEmpty()
    var filterText by remember { mutableStateOf("") }

    val filteredMerchants = remember(merchants, filterText) {
        if (filterText.isBlank()) {
            merchants
        } else {
            merchants.filter { it.name.contains(filterText, ignoreCase = true) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(
                horizontal = 24.dp,
                vertical = 16.dp
            )
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.select_merchant),
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = onDismiss
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = NovariColors.Navy
                )
            }
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        if (merchants.isNotEmpty()) {
            OutlinedTextField(
                value = filterText,
                onValueChange = { filterText = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.search_merchants)) },
                singleLine = true
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )
        }

        if (merchants.isEmpty()) {
            Text(
                text = stringResource(R.string.no_merchants_yet),
                style = MaterialTheme.typography.bodyMedium,
                color = NovariColors.Slate,
                modifier = Modifier.padding(vertical = 24.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.heightIn(max = 360.dp)
            ) {
                item {
                    MerchantItem(
                        title = stringResource(R.string.all_merchants),
                        selected = isAllSelected,
                        onClick = { onSelectionChanged(emptySet()) }
                    )
                }

                items(filteredMerchants, key = { it.key }) { merchant ->
                    MerchantItem(
                        title = merchant.name,
                        selected = merchant.key in selectedMerchantKeys,
                        onClick = {
                            val updatedSelection =
                                if (merchant.key in selectedMerchantKeys) {
                                    selectedMerchantKeys - merchant.key
                                } else {
                                    selectedMerchantKeys + merchant.key
                                }

                            onSelectionChanged(updatedSelection)
                        }
                    )
                }
            }
        }

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        Button(
            onClick = onApply,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = NovariColors.Teal
            )
        ) {
            Text(
                text = "Apply",
                style = MaterialTheme.typography.labelLarge,
                color = NovariColors.Surface
            )
        }
    }
}

@Composable
private fun MerchantItem(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(
                vertical = 10.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    if (selected) {
                        NovariColors.PaleTeal
                    } else {
                        NovariColors.SurfaceHigh
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_merchant),
                contentDescription = null,
                tint = if (selected) {
                    NovariColors.Teal
                } else {
                    NovariColors.Slate
                },
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(
            modifier = Modifier.width(12.dp)
        )

        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = NovariColors.Navy,
            modifier = Modifier.weight(1f)
        )

        Box(
            modifier = Modifier
                .size(24.dp)
                .border(
                    width = 1.5.dp,
                    color = if (selected) {
                        NovariColors.Teal
                    } else {
                        NovariColors.Border
                    },
                    shape = CircleShape
                )
                .background(
                    color = if (selected) {
                        NovariColors.Teal
                    } else {
                        Color.Transparent
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = NovariColors.Surface,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
