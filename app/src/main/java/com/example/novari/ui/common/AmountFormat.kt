package com.example.novari.ui.common

/**
 * Simple thousands-separator formatting for whole-rupee amounts
 * (e.g. 24560 -> "24,560"). Shared so Home, Add Expense, and any future
 * screen that displays a rupee amount format it identically.
 */
fun formatAmount(amount: Int): String = "%,d".format(amount)