package com.example.novari.core.model

/**
 * The "Search by" scope on SearchScreen. Decides what a typed query is
 * matched against and, for the structured fields, which picker sheet the
 * card opens.
 *
 * [MERCHANT] supports both a typed free-text query (matched against the
 * `merchant` column on TransactionEntity) and a picker sheet listing distinct
 * merchants derived from existing transactions -- there is no merchant table.
 */
enum class SearchField {
    MERCHANT,
    CATEGORY,
    AMOUNT,
    DATE
}
