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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

data class BarGroup(
    val label: String,
    val income: Double,
    val expense: Double
)

@Composable
fun ComparisonBarChart(
    barGroups: List<BarGroup>,
    modifier: Modifier = Modifier
) {
    if (barGroups.isEmpty()) return

    val maxVal = barGroups.flatMap { listOf(it.income, it.expense) }.maxOrNull()?.coerceAtLeast(100.0) ?: 100.0

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(vertical = 8.dp)
            .testTag("comparison_bar_chart")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val groupWidth = width / barGroups.size
            val barWidth = (groupWidth * 0.35f).coerceAtMost(24.dp.toPx())

            barGroups.forEachIndexed { index, group ->
                val groupCenterX = index * groupWidth + groupWidth / 2f

                // Income bar
                val incomeHeight = ((group.income / maxVal) * (height - 30.dp.toPx())).toFloat()
                val incomeX = groupCenterX - barWidth - 2.dp.toPx()
                val incomeY = height - incomeHeight - 20.dp.toPx()

                drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(incomeX, incomeY),
                    size = Size(barWidth, incomeHeight),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )

                // Expense bar
                val expenseHeight = ((group.expense / maxVal) * (height - 30.dp.toPx())).toFloat()
                val expenseX = groupCenterX + 2.dp.toPx()
                val expenseY = height - expenseHeight - 20.dp.toPx()

                drawRoundRect(
                    color = Color(0xFF666666),
                    topLeft = Offset(expenseX, expenseY),
                    size = Size(barWidth, expenseHeight),
                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                )
            }
        }
    }
}
