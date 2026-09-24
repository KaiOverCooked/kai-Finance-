package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.KaiDatabase
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.TransactionCategory
import com.example.data.local.entity.TransactionType
import com.example.data.repository.FinanceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BudgetViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FinanceRepository(KaiDatabase.getDatabase(application))

    // Real-time calculation of spent amount strictly within each budget's period, excluding transfers
    val budgets: StateFlow<List<BudgetEntity>> = combine(
        repository.allBudgets,
        repository.allTransactions
    ) { budgetList, txList ->
        budgetList.map { budget ->
            val (startMs, endMs) = repository.getPeriodStartAndEnd(budget.period)
            val realSpent = txList
                .filter {
                    it.type == TransactionType.EXPENSE &&
                            !it.isTransfer &&
                            it.category == budget.category &&
                            it.timestamp in startMs..endMs
                }
                .sumOf { it.amount }
            budget.copy(spentAmount = realSpent)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createOrUpdateBudget(category: TransactionCategory, limitAmount: Double, period: String = "Monthly") {
        viewModelScope.launch {
            repository.addBudget(
                BudgetEntity(
                    category = category,
                    limitAmount = limitAmount,
                    period = period
                )
            )
        }
    }

    fun deleteBudget(id: Long) {
        viewModelScope.launch {
            repository.deleteBudgetById(id)
        }
    }
}
