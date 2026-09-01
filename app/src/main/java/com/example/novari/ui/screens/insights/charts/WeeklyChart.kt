package com.example.novari.ui.screens.insights.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.novari.ui.theme.NovariColors

private val DotRadius = 3.dp
private val DotInnerRadius = 1.5.dp
private val StrokeWidth = 2.dp
private val HorizontalInset = 8.dp

@Composable
fun WeeklyChart(
    values: List<Float>,
    labels: List<String>,
    labelStyle: TextStyle,
    modifier: Modifier = Modifier,
    chartHeight: androidx.compose.ui.unit.Dp = 100.dp,
    labelHeight: androidx.compose.ui.unit.Dp = 20.dp
) {
    if (values.size < 2) return

    val textMeasurer = rememberTextMeasurer()
    val chartLineColor = NovariColors.ChartLine
    val dotCoreColor = NovariColors.Surface

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight)
        ) {
            val insetPx = HorizontalInset.toPx()
            val points = values.mapIndexed { index, value ->
                Offset(
                    x = xForPoint(index, values.size, size.width, insetPx),
                    y = size.height - (size.height * value)
                )
            }

            val path = Path()
            points.forEachIndexed { index, point ->
                if (index == 0) {
                    path.moveTo(point.x, point.y)
                } else {
                    val previous = points[index - 1]
                    val mid = (previous.x + point.x) / 2
                    path.cubicTo(mid, previous.y, mid, point.y, point.x, point.y)
                }
            }

            drawPath(
                path = path,
                color = chartLineColor,
                style = Stroke(width = StrokeWidth.toPx(), cap = StrokeCap.Round)
            )

            points.forEach {
                drawCircle(color = chartLineColor, radius = DotRadius.toPx(), center = it)
                drawCircle(color = dotCoreColor, radius = DotInnerRadius.toPx(), center = it)
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(labelHeight)
        ) {
            val insetPx = HorizontalInset.toPx()
            labels.forEachIndexed { index, label ->
                val x = xForPoint(index, labels.size, size.width, insetPx)
                val measured = textMeasurer.measure(
                    text = label,
                    style = labelStyle.copy(textAlign = TextAlign.Center)
                )
                drawText(
                    textLayoutResult = measured,
                    topLeft = Offset(
                        x = x - measured.size.width / 2f,
                        y = 0f
                    )
                )
            }
        }
    }
}
