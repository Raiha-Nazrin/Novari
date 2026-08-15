package com.example.novari.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.novari.ui.theme.NovariColors

data class Category(
    val name: String,
    val amount: Long,
    val percentage: Int,
    val icon: ImageVector,
    val color: Color,
    val backgroundColor: Color
)

val categories = listOf(
    Category(
        name = "Food & Dining",
        amount = 7580,
        percentage = 31,
        icon = Icons.Default.Coffee,
        color = NovariColors.Food,
        backgroundColor = NovariColors.FoodBackground
    ),
    Category(
        name = "Shopping",
        amount = 5620,
        percentage = 23,
        icon = Icons.Default.ShoppingBag,
        color = NovariColors.Shopping,
        backgroundColor = NovariColors.ShoppingBackground
    ),
    Category(
        name = "Transport",
        amount = 3450,
        percentage = 14,
        icon = Icons.Default.DirectionsCar,
        color = NovariColors.Transport,
        backgroundColor = NovariColors.TransportBackground
    ),
    Category(
        name = "Others",
        amount = 7910,
        percentage = 32,
        icon = Icons.Default.MoreHoriz,
        color = NovariColors.Others,
        backgroundColor = NovariColors.OthersBackground
    )
)
