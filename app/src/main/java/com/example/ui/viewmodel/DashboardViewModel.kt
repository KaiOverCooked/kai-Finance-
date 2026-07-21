package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.KaiDatabase
import com.example.data.local.entity.TransactionCategory
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.TransactionType
import com.example.data.repository.FinanceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class DashboardUiState(
    val totalBalance: Double = 0.0,
    val monthlyIncome: Double = 0.0,
    val monthlyExpenses: Double = 0.0,
    val netCashFlow: Double = 0.0,
    val cashFlowPoints: List<Double> = emptyList(),
    val recentTransactions: List<TransactionEntity> = emptyList(),
    val topSpendingCategories: List<Pair<TransactionCategory, Double>> = emptyList()
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FinanceRepository(KaiDatabase.getDatabase(application))

    val uiState: StateFlow<DashboardUiState> = repository.allTransactions.map { list ->
        val income = list.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val expenses = list.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val total = income - expenses

        val recent = list.take(5)

        val points = if (list.isNotEmpty()) {
            var running = 0.0
            list.reversed().map { tx ->
                if (tx.type == TransactionType.INCOME) running += tx.amount
                else running -= tx.amount
                running
            }
        } else listOf(0.0, 500.0, 1200.0, 2400.0, 3100.0)

        val categoryMap = list.filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second }
            .take(4)

        DashboardUiState(
            totalBalance = total,
            monthlyIncome = income,
            monthlyExpenses = expenses,
            netCashFlow = income - expenses,
            cashFlowPoints = points,
            recentTransactions = recent,
            topSpendingCategories = categoryMap
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )
}
