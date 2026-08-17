package com.example.novari.ui.screens.insights.charts

internal fun xForPoint(index: Int, count: Int, width: Float, insetPx: Float): Float {
    if (count <= 1) return width / 2f
    val usable = width - 2 * insetPx
    return insetPx + index * (usable / (count - 1))
}
