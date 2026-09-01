package com.example.novari.ui.model

import androidx.annotation.DrawableRes
import com.example.novari.core.model.TransactionType

/**
 * A single transaction as rendered in a list row. Shared between HomeScreen's
 * "Recent transactions" and TransactionListScreen so both read the same shape
 * off the same repository query.
 *
 * [iconRes] is a drawable resource from the project's res/drawable folder.
 * [type] distinguishes income from expense so the row can be styled
 * accordingly (e.g. a "+" prefix and different color for income).
 */
data class Transaction(
    val id: String,
    val title: String,
    val category: String,
    val date: String,
    val amount: Int,
    @DrawableRes val iconRes: Int,
    val type: TransactionType = TransactionType.EXPENSE,
    val isAutoDetected: Boolean = false
)
