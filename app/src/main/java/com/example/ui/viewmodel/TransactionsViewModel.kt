package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.KaiDatabase
import com.example.data.local.entity.TransactionCategory
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.TransactionType
import com.example.data.repository.FinanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class TransactionTypeFilter { ALL, EXPENSE, INCOME }

class TransactionsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FinanceRepository(KaiDatabase.getDatabase(application))

    val searchQuery = MutableStateFlow("")
    val selectedTypeFilter = MutableStateFlow(TransactionTypeFilter.ALL)
    val selectedCategoryFilter = MutableStateFlow<TransactionCategory?>(null)

    val filteredTransactions: StateFlow<List<TransactionEntity>> = combine(
        repository.allTransactions,
        searchQuery,
        selectedTypeFilter,
        selectedCategoryFilter
    ) { list, query, typeFilter, categoryFilter ->
        list.filter { tx ->
            val matchesQuery = query.isBlank() || tx.title.contains(query, ignoreCase = true) || tx.note.contains(query, ignoreCase = true)
            val matchesType = when (typeFilter) {
                TransactionTypeFilter.ALL -> true
                TransactionTypeFilter.EXPENSE -> tx.type == TransactionType.EXPENSE
                TransactionTypeFilter.INCOME -> tx.type == TransactionType.INCOME
            }
            val matchesCategory = categoryFilter == null || tx.category == categoryFilter
            matchesQuery && matchesType && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addTransaction(
        title: String,
        amount: Double,
        type: TransactionType,
        category: TransactionCategory,
        note: String,
        imageUri: String? = null
    ) {
        viewModelScope.launch {
            repository.addTransaction(
                TransactionEntity(
                    title = title,
                    amount = amount,
                    type = type,
                    category = category,
                    note = note,
                    imageUri = imageUri
                )
            )
        }
    }

    fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }
}
