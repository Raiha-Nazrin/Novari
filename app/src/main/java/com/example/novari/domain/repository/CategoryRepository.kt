package com.example.novari.domain.repository

import com.example.novari.core.database.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

sealed interface AddCategoryResult {
    data class Success(val id: String) : AddCategoryResult
    data object BlankName : AddCategoryResult
    data object DuplicateName : AddCategoryResult
}

interface CategoryRepository {
    fun observeActive(): Flow<List<CategoryEntity>>
    suspend fun findById(id: String): CategoryEntity?
    suspend fun addUserCategory(name: String): AddCategoryResult
    suspend fun rename(id: String, newName: String): AddCategoryResult
    suspend fun deactivate(id: String)
}
