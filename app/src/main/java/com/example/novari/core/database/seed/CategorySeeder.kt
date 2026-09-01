package com.example.novari.core.database.seed

import com.example.novari.core.database.dao.CategoryDao
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategorySeeder @Inject constructor(
    private val categoryDao: CategoryDao
) {
    suspend fun seedIfNeeded() {
        val now = System.currentTimeMillis()
        categoryDao.insertAll(DEFAULT_CATEGORIES.map { it.toEntity(now) })
    }
}
