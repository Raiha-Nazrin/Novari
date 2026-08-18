package com.example.novari.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * Minimal placeholder screens for destinations that only need to exist for
 * navigation purposes right now. Each establishes the composable entry
 * point future work will flesh out — no functionality beyond a label.
 */

//@Composable
//fun InsightsScreen(modifier: Modifier = Modifier) {
//    PlaceholderContent(text = "Insights", modifier = modifier)
//}

@Composable
fun AddExpenseScreen(modifier: Modifier = Modifier) {
    PlaceholderContent(text = "Add Expense", modifier = modifier)
}

@Composable
private fun PlaceholderContent(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = MaterialTheme.typography.headlineSmall)
    }
}