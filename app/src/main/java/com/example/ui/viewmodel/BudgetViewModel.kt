package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.KaiDatabase
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.TransactionCategory
import com.example.data.repository.FinanceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BudgetViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FinanceRepository(KaiDatabase.getDatabase(application))

    val budgets: StateFlow<List<BudgetEntity>> = repository.allBudgets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createOrUpdateBudget(category: TransactionCategory, limitAmount: Double) {
        viewModelScope.launch {
            repository.addBudget(
                BudgetEntity(
                    category = category,
                    limitAmount = limitAmount
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
