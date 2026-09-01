package com.example.novari.core.database.seed

import com.example.novari.core.database.entity.CategoryEntity

data class DefaultCategory(
    val id: String,
    val name: String,
    val iconKey: String,
    val colorKey: String?,
    val sortOrder: Int
)

val DEFAULT_CATEGORIES = listOf(
    DefaultCategory(id = "cat_food", name = "Food", iconKey = "food", colorKey = "food", sortOrder = 0),
    DefaultCategory(id = "cat_transport", name = "Transport", iconKey = "transport", colorKey = "transport", sortOrder = 1),
    DefaultCategory(id = "cat_shopping", name = "Shopping", iconKey = "shopping", colorKey = "shopping", sortOrder = 2),
    DefaultCategory(id = "cat_health", name = "Health", iconKey = "health", colorKey = null, sortOrder = 3),
    DefaultCategory(id = "cat_entertainment", name = "Entertainment", iconKey = "entertainment", colorKey = null, sortOrder = 4),
    DefaultCategory(id = "cat_travel", name = "Travel", iconKey = "travel", colorKey = null, sortOrder = 5),
    DefaultCategory(id = "cat_other", name = "Other", iconKey = "other", colorKey = "others", sortOrder = 6)
)

fun DefaultCategory.toEntity(now: Long): CategoryEntity = CategoryEntity(
    id = id,
    name = name,
    iconKey = iconKey,
    isSystem = true,
    isActive = true,
    createdAt = now,
    updatedAt = now,
    colorKey = colorKey,
    sortOrder = sortOrder
)
