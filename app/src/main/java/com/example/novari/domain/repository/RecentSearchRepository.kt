package com.example.novari.domain.repository

import com.example.novari.core.model.SearchField
import kotlinx.coroutines.flow.Flow

/** One persisted recent search, newest first. */
data class RecentSearch(
    val query: String,
    val searchField: SearchField
)

interface RecentSearchRepository {
    fun observeRecent(): Flow<List<RecentSearch>>
    suspend fun record(query: String, searchField: SearchField)
    suspend fun remove(query: String)
    suspend fun clearAll()
}
