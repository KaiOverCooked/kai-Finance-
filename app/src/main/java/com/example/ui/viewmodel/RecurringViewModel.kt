package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.KaiDatabase
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.RecurringCategoryType
import com.example.data.local.entity.RecurringEntity
import com.example.data.local.entity.RecurringFrequency
import com.example.data.local.entity.TransactionCategory
import com.example.data.local.entity.TransactionType
import com.example.data.repository.FinanceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RecurringViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = FinanceRepository(KaiDatabase.getDatabase(application))

    val recurringList: StateFlow<List<RecurringEntity>> = repository.allRecurring
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val accounts: StateFlow<List<AccountEntity>> = repository.allAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Automatically check and process pending recurring transactions on initialization
        checkAndProcessPendingRecurring()
    }

    fun checkAndProcessPendingRecurring() {
        viewModelScope.launch {
            repository.processDueRecurringTransactions()
        }
    }

    fun createRecurring(
        title: String,
        amount: Double,
        type: TransactionType,
        category: TransactionCategory,
        recurringCategory: RecurringCategoryType,
        frequency: RecurringFrequency,
        nextDueDateMillis: Long,
        autoExecute: Boolean,
        note: String,
        accountId: Long?
    ) {
        viewModelScope.launch {
            repository.addRecurring(
                RecurringEntity(
                    title = title,
                    amount = amount,
                    type = type,
                    category = category,
                    recurringCategory = recurringCategory,
                    frequency = frequency,
                    nextDueDateMillis = nextDueDateMillis,
                    autoExecute = autoExecute,
                    note = note,
                    accountId = accountId
                )
            )
        }
    }

    fun executeNow(id: Long, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val res = repository.executeRecurringItem(id)
            onResult(res)
        }
    }

    fun deleteRecurring(id: Long) {
        viewModelScope.launch {
            repository.deleteRecurringById(id)
        }
    }
}
