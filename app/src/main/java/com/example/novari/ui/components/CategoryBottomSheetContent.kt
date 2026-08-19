package com.example.novari.ui.components

import android.widget.Space
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.novari.R
import com.example.novari.ui.theme.NovariColors
import com.example.novari.ui.theme.NovariTypography

@Composable
fun CategoryBottomSheetContent(
    selectedCategories: Set<String>,
    onSelectionChanged: (Set<String>) -> Unit,
    onApply: () -> Unit,
    onDismiss: () -> Unit
) {
    val categories = listOf(
        "Food",
        "Shopping",
        "Transport",
        "Others"
    )

    val isAllSelected = selectedCategories.isEmpty()

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
                text = "Select Category",
                style = NovariTypography.headlineSmall
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

        // All categories
        CategoryItem(
            title = "All",
            selected = isAllSelected,
            onClick = {
                onSelectionChanged(emptySet())
            }
        )

        categories.forEach { category ->

            CategoryItem(
                title = category,
                selected = category in selectedCategories,
                onClick = {

                    val updatedSelection =
                        if (category in selectedCategories) {
                            selectedCategories - category
                        } else {
                            selectedCategories + category
                        }

                    onSelectionChanged(updatedSelection)
                }
            )
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
                style = NovariTypography.labelLarge,
                color = Color.White
            )
        }
    }
}


@Composable
private fun CategoryItem(
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
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(
                    id = R.drawable.ic_category
                ),
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
            style = NovariTypography.labelLarge,
            color = NovariColors.Navy,
            modifier = Modifier.weight(1f)
        )

        // Selection indicator
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
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}