package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.KaiDatabase
import com.example.data.local.entity.TransactionCategory
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.TransactionType
import com.example.data.repository.FinanceRepository
import com.example.ui.components.BarGroup
import com.example.ui.components.PieSegment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

enum class TimePeriod { DAILY, WEEKLY, MONTHLY, YEARLY }

data class AnalyticsUiState(
    val timePeriod: TimePeriod = TimePeriod.MONTHLY,
    val totalExpense: Double = 0.0,
    val totalIncome: Double = 0.0,
    val pieSegments: List<PieSegment> = emptyList(),
    val barGroups: List<BarGroup> = emptyList(),
    val categoryBreakdown: List<Pair<TransactionCategory, Double>> = emptyList()
)

class AnalyticsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FinanceRepository(KaiDatabase.getDatabase(application))
    val selectedPeriod = MutableStateFlow(TimePeriod.MONTHLY)

    val uiState: StateFlow<AnalyticsUiState> = combine(
        repository.allTransactions,
        selectedPeriod
    ) { list, period ->
        val filteredList = filterTransactionsByPeriod(list, period)
        val expenseList = filteredList.filter { it.type == TransactionType.EXPENSE }
        val incomeList = filteredList.filter { it.type == TransactionType.INCOME }

        val totalExp = expenseList.sumOf { it.amount }
        val totalInc = incomeList.sumOf { it.amount }

        val categorySums = expenseList.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        val pieSegments = categorySums.map { (category, sum) ->
            PieSegment(
                categoryName = category.name,
                value = sum,
                color = androidx.compose.ui.graphics.Color.Unspecified
            )
        }

        val barGroups = listOf(
            BarGroup("W1", totalInc * 0.2, totalExp * 0.25),
            BarGroup("W2", totalInc * 0.3, totalExp * 0.20),
            BarGroup("W3", totalInc * 0.25, totalExp * 0.35),
            BarGroup("W4", totalInc * 0.25, totalExp * 0.20)
        )

        AnalyticsUiState(
            timePeriod = period,
            totalExpense = totalExp,
            totalIncome = totalInc,
            pieSegments = pieSegments,
            barGroups = barGroups,
            categoryBreakdown = categorySums.toList().sortedByDescending { it.second }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AnalyticsUiState())

    private fun filterTransactionsByPeriod(list: List<TransactionEntity>, period: TimePeriod): List<TransactionEntity> {
        val now = System.currentTimeMillis()
        val dayMs = 24 * 60 * 60 * 1000L
        val limitMs = when (period) {
            TimePeriod.DAILY -> 1 * dayMs
            TimePeriod.WEEKLY -> 7 * dayMs
            TimePeriod.MONTHLY -> 30 * dayMs
            TimePeriod.YEARLY -> 365 * dayMs
        }
        return list.filter { (now - it.timestamp) <= limitMs }
    }
}
