package com.example.novari.data.repository

import com.example.novari.core.database.dao.CategoryDao
import com.example.novari.core.database.entity.CategoryEntity
import com.example.novari.core.util.TransactionId
import com.example.novari.domain.repository.AddCategoryResult
import com.example.novari.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoomCategoryRepository @Inject constructor(
    private val dao: CategoryDao
) : CategoryRepository {

    override fun observeActive(): Flow<List<CategoryEntity>> =
        dao.observeActive()

    override suspend fun findById(id: String): CategoryEntity? =
        dao.findById(id)

    override suspend fun addUserCategory(name: String): AddCategoryResult {
        val trimmed = name.trim()
        if (trimmed.isBlank()) return AddCategoryResult.BlankName

        val existing = dao.findByName(trimmed)
        if (existing != null) {
            if (existing.isActive) return AddCategoryResult.DuplicateName
            dao.reactivate(existing.id, System.currentTimeMillis())
            return AddCategoryResult.Success(existing.id)
        }

        val now = System.currentTimeMillis()
        val id = TransactionId.new()
        dao.insert(
            CategoryEntity(
                id = id,
                name = trimmed,
                iconKey = null,
                isSystem = false,
                isActive = true,
                createdAt = now,
                updatedAt = now,
                colorKey = null,
                sortOrder = 100
            )
        )
        return AddCategoryResult.Success(id)
    }

    override suspend fun rename(id: String, newName: String): AddCategoryResult {
        val trimmed = newName.trim()
        if (trimmed.isBlank()) return AddCategoryResult.BlankName

        val current = dao.findById(id) ?: return AddCategoryResult.BlankName
        if (current.isSystem) return AddCategoryResult.DuplicateName

        val existing = dao.findByName(trimmed)
        if (existing != null && existing.id != id) return AddCategoryResult.DuplicateName

        dao.update(current.copy(name = trimmed, updatedAt = System.currentTimeMillis()))
        return AddCategoryResult.Success(id)
    }

    override suspend fun deactivate(id: String) {
        dao.deactivate(id, System.currentTimeMillis())
    }
}
