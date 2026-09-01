package com.example.novari.ui.screens.insights.charts

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.novari.ui.theme.NovariColors

/**
 * Insets the plotted line so the largest drawn marker (the selection halo)
 * never lands exactly on the canvas edge and gets clipped.
 */
private val HorizontalInset = 12.dp
private val DotRadius = 4.dp
private val SelectedHaloRadius = 10.dp
private val StrokeWidth = 2.dp

@Composable
fun SpendingChart(
    points: List<Float>,
    modifier: Modifier = Modifier,
    selectedIndex: Int? = null
) {
    if (points.size < 2) return

    val chartLineColor = NovariColors.ChartLine
    val chartGridColor = NovariColors.ChartGrid
    val dotCoreColor = NovariColors.Surface

    Canvas(modifier = modifier) {
        val insetPx = HorizontalInset.toPx()
        val width = size.width
        val height = size.height

        val chartTop = SelectedHaloRadius.toPx()
        val chartBottom = height - SelectedHaloRadius.toPx()

        val coordinates = points.mapIndexed { index, value ->
            Offset(
                x = xForPoint(index, points.size, width, insetPx),
                y = chartBottom - ((chartBottom - chartTop) * value)
            )
        }

        val areaPath = Path().apply {
            moveTo(coordinates.first().x, chartBottom)
            coordinates.forEach { lineTo(it.x, it.y) }
            lineTo(coordinates.last().x, chartBottom)
            close()
        }

        drawPath(
            path = areaPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    chartLineColor.copy(alpha = 0.18f),
                    Color.Transparent
                )
            )
        )

        val linePath = Path()
        coordinates.forEachIndexed { index, point ->
            if (index == 0) {
                linePath.moveTo(point.x, point.y)
            } else {
                val previous = coordinates[index - 1]
                val midX = (previous.x + point.x) / 2
                linePath.cubicTo(midX, previous.y, midX, point.y, point.x, point.y)
            }
        }

        drawPath(
            path = linePath,
            color = chartLineColor,
            style = Stroke(width = StrokeWidth.toPx(), cap = StrokeCap.Round)
        )

        selectedIndex?.let { index ->
            if (index in coordinates.indices) {
                val selected = coordinates[index]

                drawCircle(
                    color = chartLineColor.copy(alpha = 0.18f),
                    radius = SelectedHaloRadius.toPx(),
                    center = selected
                )
                drawCircle(
                    color = chartLineColor,
                    radius = DotRadius.toPx(),
                    center = selected
                )
                drawCircle(
                    color = dotCoreColor,
                    radius = DotRadius.toPx() / 2,
                    center = selected
                )
            }
        }

        drawLine(
            color = chartGridColor,
            start = Offset(0f, chartBottom),
            end = Offset(width, chartBottom),
            strokeWidth = 1.dp.toPx()
        )
    }
}
