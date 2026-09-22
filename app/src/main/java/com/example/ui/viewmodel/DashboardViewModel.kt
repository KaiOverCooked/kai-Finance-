package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.KaiDatabase
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.DebtEntity
import com.example.data.local.entity.DebtType
import com.example.data.local.entity.InvestmentEntity
import com.example.data.local.entity.RecurringEntity
import com.example.data.local.entity.TransactionCategory
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.TransactionType
import com.example.data.repository.FinanceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class DashboardUiState(
    val totalBalance: Double = 0.0,
    val monthlyIncome: Double = 0.0,
    val monthlyExpenses: Double = 0.0,
    val netCashFlow: Double = 0.0,
    val cashFlowPoints: List<Double> = emptyList(),
    val recentTransactions: List<TransactionEntity> = emptyList(),
    val topSpendingCategories: List<Pair<TransactionCategory, Double>> = emptyList(),
    val accounts: List<AccountEntity> = emptyList(),
    val totalPiutang: Double = 0.0,
    val totalHutang: Double = 0.0,
    val totalInvestmentsValue: Double = 0.0,
    val totalInvestmentsProfit: Double = 0.0,
    val activeRecurringCount: Int = 0
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FinanceRepository(KaiDatabase.getDatabase(application))

    val uiState: StateFlow<DashboardUiState> = combine(
        repository.allTransactions,
        repository.allAccounts,
        repository.allDebts,
        repository.allInvestments,
        repository.allRecurring
    ) { txList, accountsList, debtsList, invList, recList ->
        val income = txList.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val expenses = txList.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val accountsTotal = accountsList.sumOf { it.balance }
        val netBalance = if (accountsList.isNotEmpty()) accountsTotal else (income - expenses)

        val recent = txList.take(5)

        val points = if (txList.isNotEmpty()) {
            var running = 0.0
            txList.reversed().map { tx ->
                if (tx.type == TransactionType.INCOME) running += tx.amount
                else running -= tx.amount
                running
            }
        } else listOf(0.0, 500.0, 1200.0, 2400.0, 3100.0)

        val categoryMap = txList.filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second }
            .take(4)

        val piutang = debtsList.filter { it.type == DebtType.PIUTANG && !it.isSettled }.sumOf { it.amount - it.paidAmount }
        val hutang = debtsList.filter { it.type == DebtType.HUTANG && !it.isSettled }.sumOf { it.amount - it.paidAmount }
        val invValue = invList.sumOf { it.currentValue }
        val invCost = invList.sumOf { it.totalCost }
        val invProfit = invValue - invCost

        DashboardUiState(
            totalBalance = netBalance,
            monthlyIncome = income,
            monthlyExpenses = expenses,
            netCashFlow = income - expenses,
            cashFlowPoints = points,
            recentTransactions = recent,
            topSpendingCategories = categoryMap,
            accounts = accountsList,
            totalPiutang = piutang,
            totalHutang = hutang,
            totalInvestmentsValue = invValue,
            totalInvestmentsProfit = invProfit,
            activeRecurringCount = recList.count { it.isActive }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )
}
