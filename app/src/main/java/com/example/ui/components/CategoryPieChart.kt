package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class PieSegment(
    val categoryName: String,
    val value: Double,
    val color: Color
)

@Composable
fun CategoryPieChart(
    segments: List<PieSegment>,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 160.dp,
    centerContent: @Composable (() -> Unit)? = null
) {
    val total = segments.sumOf { it.value }.coerceAtLeast(1.0)
    val monochromeColors = listOf(
        Color(0xFFFFFFFF),
        Color(0xFFD0D0D0),
        Color(0xFFA0A0A0),
        Color(0xFF707070),
        Color(0xFF404040),
        Color(0xFF252525)
    )

    Box(
        modifier = modifier
            .size(sizeDp)
            .testTag("category_pie_chart"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(sizeDp)) {
            var startAngle = -90f
            val strokeWidth = 24.dp.toPx()

            segments.forEachIndexed { index, segment ->
                val sweepAngle = ((segment.value / total) * 360f).toFloat()
                val segColor = if (segment.color != Color.Unspecified) segment.color else monochromeColors[index % monochromeColors.size]

                drawArc(
                    color = segColor,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth)
                )

                startAngle += sweepAngle
            }
        }

        centerContent?.invoke()
    }
}
