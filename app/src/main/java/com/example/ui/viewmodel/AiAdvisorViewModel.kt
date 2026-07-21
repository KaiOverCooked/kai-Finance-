package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.KaiDatabase
import com.example.data.local.entity.TransactionCategory
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.TransactionType
import com.example.data.remote.GeminiService
import com.example.data.remote.ParsedReceiptResult
import com.example.data.repository.FinanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class AiAdvisorUiState(
    val isLoading: Boolean = false,
    val analysisReport: String? = null,
    val scannedReceiptResult: ParsedReceiptResult? = null,
    val errorMessage: String? = null
)

class AiAdvisorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FinanceRepository(KaiDatabase.getDatabase(application))
    private val geminiService = GeminiService(application)

    private val _uiState = MutableStateFlow(AiAdvisorUiState())
    val uiState = _uiState.asStateFlow()

    fun runHighReasoningAudit() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val transactions = repository.allTransactions.first()
            val goals = repository.allGoals.first()

            val income = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val expenses = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            val netWorth = income - expenses

            val topCategories = transactions.filter { it.type == TransactionType.EXPENSE }
                .groupBy { it.category }
                .map { "${it.key.name}: $${it.value.sumOf { tx -> tx.amount }}" }
                .take(3)
                .joinToString(", ")

            val activeGoalsStr = goals.joinToString(", ") { "${it.name} ($${it.currentAmount}/$${it.targetAmount})" }

            val report = geminiService.analyzeFinancialHealthWithHighThinking(
                totalIncome = income,
                totalExpenses = expenses,
                netWorth = netWorth,
                topExpenseCategories = if (topCategories.isBlank()) "None" else topCategories,
                activeGoals = if (activeGoalsStr.isBlank()) "None" else activeGoalsStr
            )

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                analysisReport = report
            )
        }
    }

    fun scanReceiptImage(bitmap: Bitmap) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = geminiService.parseReceiptImage(bitmap)
            if (result != null) {
                _uiState.value = _uiState.value.copy(isLoading = false, scannedReceiptResult = result)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Could not parse receipt image. Please enter manually or check API key."
                )
            }
        }
    }

    fun clearScannedResult() {
        _uiState.value = _uiState.value.copy(scannedReceiptResult = null)
    }

    fun saveScannedReceiptAsTransaction(result: ParsedReceiptResult) {
        viewModelScope.launch {
            repository.addTransaction(
                TransactionEntity(
                    title = result.title,
                    amount = result.amount,
                    type = result.type,
                    category = result.category,
                    note = result.note
                )
            )
            clearScannedResult()
        }
    }
}
