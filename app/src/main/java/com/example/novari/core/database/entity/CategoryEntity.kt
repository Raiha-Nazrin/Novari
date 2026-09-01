package com.example.novari.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "categories",
    indices = [Index(value = ["name"], unique = true)]
)
data class CategoryEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val iconKey: String?,
    val isSystem: Boolean,
    val isActive: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val colorKey: String? = null,
    val sortOrder: Int = 100
)
