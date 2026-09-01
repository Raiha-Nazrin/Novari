package com.example.novari.ui.model

import androidx.annotation.DrawableRes
import com.example.novari.core.database.entity.CategoryEntity
import com.example.novari.core.database.entity.iconFor

data class CategoryUiModel(
    val id: String,
    val name: String,
    @DrawableRes val iconRes: Int
)

fun CategoryEntity.toUiModel(): CategoryUiModel = CategoryUiModel(
    id = id,
    name = name,
    iconRes = iconFor()
)
