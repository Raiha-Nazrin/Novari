package com.example.novari.core.database.entity

import androidx.annotation.DrawableRes
import com.example.novari.R

@DrawableRes
fun CategoryEntity?.iconFor(): Int = when (this?.iconKey) {
    "food" -> R.drawable.ic_food
    "transport" -> R.drawable.ic_transport
    "shopping" -> R.drawable.ic_shopping
    "health" -> R.drawable.ic_health
    "entertainment" -> R.drawable.ic_entertainment
    "travel" -> R.drawable.ic_travel
    "other" -> R.drawable.ic_other
    else -> R.drawable.ic_category
}
