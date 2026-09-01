package com.example.novari.data.repository

import com.example.novari.core.database.dao.RecentSearchDao
import com.example.novari.core.database.entity.RecentSearchEntity
import com.example.novari.core.model.SearchField
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * In-memory stand-in for [RecentSearchDao] that mirrors the real query's semantics.
 *
 * [rows] always stays in raw insertion order -- only [sortedView] computes the
 * newest-first ordering (breaking searchedAt ties by insertion recency, as
 * SQLite's rowid would). Re-sorting the storage itself on every write would
 * make that tie-break non-idempotent: a second sort of an already-sorted list
 * reverses ties instead of preserving them.
 */
private class FakeRecentSearchDao : RecentSearchDao {
    private val rows = MutableStateFlow<List<RecentSearchEntity>>(emptyList())
    var trimCalledWith: Int? = null

    private fun sortedView(): List<RecentSearchEntity> =
        rows.value.asReversed().sortedByDescending { it.searchedAt }

    override suspend fun upsert(entity: RecentSearchEntity) {
        rows.value = rows.value
            .filterNot { it.query.equals(entity.query, ignoreCase = true) } + entity
    }

    override fun observeRecent(limit: Int): Flow<List<RecentSearchEntity>> =
        MutableStateFlow(sortedView().take(limit))

    override suspend fun deleteByQuery(query: String) {
        rows.value = rows.value.filterNot { it.query.equals(query, ignoreCase = true) }
    }

    override suspend fun clearAll() {
        rows.value = emptyList()
    }

    override suspend fun trimTo(limit: Int) {
        trimCalledWith = limit
        val survivingIds = sortedView().take(limit).map { it.id }.toSet()
        rows.value = rows.value.filter { it.id in survivingIds }
    }
}

class RoomRecentSearchRepositoryTest {

    @Test
    fun `record ignores a blank query`() = runTest {
        val dao = FakeRecentSearchDao()
        val repository = RoomRecentSearchRepository(dao)

        repository.record("   ", SearchField.MERCHANT)

        assertTrue(repository.observeRecent().first().isEmpty())
    }

    @Test
    fun `recording an existing query moves it to the top instead of duplicating`() = runTest {
        val dao = FakeRecentSearchDao()
        val repository = RoomRecentSearchRepository(dao)

        repository.record("Swiggy", SearchField.MERCHANT)
        delay(1)
        repository.record("Amazon", SearchField.MERCHANT)
        delay(1)
        repository.record("swiggy", SearchField.MERCHANT)

        val recent = repository.observeRecent().first()
        assertEquals(2, recent.size)
        assertEquals("swiggy", recent.first().query)
    }

    @Test
    fun `record caps the list at five entries`() = runTest {
        val dao = FakeRecentSearchDao()
        val repository = RoomRecentSearchRepository(dao)

        listOf("a", "b", "c", "d", "e", "f").forEach {
            repository.record(it, SearchField.MERCHANT)
            delay(1)
        }

        assertEquals(5, dao.trimCalledWith)
    }

    @Test
    fun `remove deletes a single recent search`() = runTest {
        val dao = FakeRecentSearchDao()
        val repository = RoomRecentSearchRepository(dao)
        repository.record("Swiggy", SearchField.MERCHANT)
        repository.record("Amazon", SearchField.MERCHANT)

        repository.remove("Swiggy")

        val recent = repository.observeRecent().first()
        assertEquals(1, recent.size)
        assertEquals("Amazon", recent.single().query)
    }

    @Test
    fun `clearAll removes every recent search`() = runTest {
        val dao = FakeRecentSearchDao()
        val repository = RoomRecentSearchRepository(dao)
        repository.record("Swiggy", SearchField.MERCHANT)

        repository.clearAll()

        assertTrue(repository.observeRecent().first().isEmpty())
    }
}
