package com.example.novari.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.novari.core.database.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Insert
    suspend fun insert(entity: CategoryEntity)

    @Update
    suspend fun update(entity: CategoryEntity)

    @Query("SELECT * FROM categories WHERE isActive = 1 ORDER BY name")
    fun observeActive(): Flow<List<CategoryEntity>>
}
