package com.example.novari.ui.screens.search

import com.example.novari.core.database.entity.CategoryEntity
import com.example.novari.core.database.entity.TransactionEntity
import com.example.novari.ui.model.Transaction
import com.example.novari.ui.screens.home.toHomeTransaction
import java.time.LocalDate

/** Maps search-result entities to the row shape SearchScreen renders. */
fun List<TransactionEntity>.toSearchResults(
    categoriesById: Map<String, CategoryEntity>,
    today: LocalDate = LocalDate.now()
): List<Transaction> = map { it.toHomeTransaction(categoriesById, today) }
