package com.example.novari.data.repository

import com.example.novari.core.database.dao.RecentSearchDao
import com.example.novari.core.database.entity.RecentSearchEntity
import com.example.novari.core.model.SearchField
import com.example.novari.core.util.TransactionId
import com.example.novari.domain.repository.RecentSearch
import com.example.novari.domain.repository.RecentSearchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private const val MAX_RECENT_SEARCHES = 5

@Singleton
class RoomRecentSearchRepository @Inject constructor(
    private val dao: RecentSearchDao
) : RecentSearchRepository {

    override fun observeRecent(): Flow<List<RecentSearch>> =
        dao.observeRecent(MAX_RECENT_SEARCHES).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun record(query: String, searchField: SearchField) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return

        dao.upsert(
            RecentSearchEntity(
                id = TransactionId.new(),
                query = trimmed,
                searchField = searchField.name,
                searchedAt = System.currentTimeMillis()
            )
        )
        dao.trimTo(MAX_RECENT_SEARCHES)
    }

    override suspend fun remove(query: String) {
        dao.deleteByQuery(query)
    }

    override suspend fun clearAll() {
        dao.clearAll()
    }
}

private fun RecentSearchEntity.toDomain(): RecentSearch = RecentSearch(
    query = query,
    searchField = runCatching { SearchField.valueOf(searchField) }.getOrDefault(SearchField.MERCHANT)
)
