package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.TransactionCategory
import com.example.ui.components.CategoryPieChart
import com.example.ui.components.ComparisonBarChart
import com.example.ui.components.GlassCard
import com.example.ui.components.GoalProgressBar
import com.example.ui.viewmodel.AnalyticsUiState
import com.example.ui.viewmodel.TimePeriod
import java.util.Locale

@Composable
fun AnalyticsScreen(
    state: AnalyticsUiState,
    selectedPeriod: TimePeriod,
    onPeriodChange: (TimePeriod) -> Unit,
    currencySymbol: String
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .testTag("analytics_screen")
    ) {
        // Header
        Text(
            text = "INTELLIGENCE",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Analytics & Breakdown",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Time Period Filter Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TimePeriod.entries.forEach { period ->
                FilterChip(
                    selected = selectedPeriod == period,
                    onClick = { onPeriodChange(period) },
                    label = { Text(period.name.lowercase().replaceFirstChar { it.uppercase() }) },
                    modifier = Modifier.testTag("period_${period.name}")
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Category Pie Chart Card
        GlassCard(
            modifier = Modifier.padding(bottom = 16.dp),
            testTag = "analytics_pie_card"
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "EXPENSE CATEGORY DISTRIBUTION",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (state.pieSegments.isEmpty()) {
                    Text(
                        text = "No expenses recorded in this period.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    CategoryPieChart(
                        segments = state.pieSegments,
                        centerContent = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Total",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "$currencySymbol${String.format(Locale.US, "%,.0f", state.totalExpense)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    )
                }
            }
        }

        // Comparison Bar Chart Card
        GlassCard(
            modifier = Modifier.padding(bottom = 16.dp),
            testTag = "analytics_bar_card"
        ) {
            Column {
                Text(
                    text = "INCOME VS EXPENSE COMPARISON",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                ComparisonBarChart(barGroups = state.barGroups)
            }
        }

        // Detailed Category Breakdown List
        Text(
            text = "Expense Breakdown",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        state.categoryBreakdown.forEach { (category, amount) ->
            val ratio = if (state.totalExpense > 0) (amount / state.totalExpense).toFloat() else 0f
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                testTag = "breakdown_${category.name}"
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = category.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$currencySymbol${String.format(Locale.US, "%,.2f", amount)} (${(ratio * 100).toInt()}%)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    GoalProgressBar(progress = ratio)
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}
