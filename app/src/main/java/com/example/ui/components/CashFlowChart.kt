package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
fun CashFlowChart(
    dataPoints: List<Double>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.onSurface
) {
    if (dataPoints.isEmpty()) return

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(vertical = 12.dp)
            .testTag("cash_flow_chart")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val maxVal = (dataPoints.maxOrNull() ?: 1.0).coerceAtLeast(1.0)
            val minVal = (dataPoints.minOrNull() ?: 0.0).coerceAtMost(0.0)
            val range = (maxVal - minVal).coerceAtLeast(1.0)

            val spacing = width / (dataPoints.size - 1).coerceAtLeast(1)

            // Draw faint grid lines
            val gridColor = lineColor.copy(alpha = 0.1f)
            for (i in 1..3) {
                val y = height * (i / 4f)
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            val path = Path()
            val fillPath = Path()

            dataPoints.forEachIndexed { index, value ->
                val x = index * spacing
                val y = height - ((value - minVal) / range * (height - 20.dp.toPx())).toFloat() - 10.dp.toPx()

                if (index == 0) {
                    path.moveTo(x, y)
                    fillPath.moveTo(x, height)
                    fillPath.lineTo(x, y)
                } else {
                    val prevX = (index - 1) * spacing
                    val prevY = height - ((dataPoints[index - 1] - minVal) / range * (height - 20.dp.toPx())).toFloat() - 10.dp.toPx()

                    val controlX1 = prevX + (x - prevX) / 2f
                    val controlY1 = prevY
                    val controlX2 = prevX + (x - prevX) / 2f
                    val controlY2 = y

                    path.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
                    fillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
                }

                if (index == dataPoints.size - 1) {
                    fillPath.lineTo(x, height)
                    fillPath.close()
                }

                // Draw data points
                drawCircle(
                    color = lineColor,
                    radius = 4.dp.toPx(),
                    center = Offset(x, y)
                )
                drawCircle(
                    color = Color.Black,
                    radius = 2.dp.toPx(),
                    center = Offset(x, y)
                )
            }

            // Fill area gradient
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(lineColor.copy(alpha = 0.25f), Color.Transparent)
                )
            )

            // Draw line stroke
            drawPath(
                path = path,
                color = lineColor,
                style = Stroke(width = 3.dp.toPx())
            )
        }
    }
}
