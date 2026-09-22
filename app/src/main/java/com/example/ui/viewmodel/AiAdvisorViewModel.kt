package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.KaiDatabase
import com.example.data.local.entity.DebtType
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

data class ChatMessage(
    val isUser: Boolean,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class AiAdvisorUiState(
    val isLoading: Boolean = false,
    val isChatLoading: Boolean = false,
    val analysisReport: String? = null,
    val scannedReceiptResult: ParsedReceiptResult? = null,
    val chatMessages: List<ChatMessage> = listOf(
        ChatMessage(
            isUser = false,
            text = "Halo, saya Kai AI Financial Strategist Anda. Saya dapat menganalisis arus kas, anggaran, portofolio investasi, serta hutang/piutang Anda secara real-time. Tanyakan apa saja tentang kondisi finansial Anda!"
        )
    ),
    val errorMessage: String? = null
)

class AiAdvisorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FinanceRepository(KaiDatabase.getDatabase(application))
    private val geminiService = GeminiService(application)

    private val _uiState = MutableStateFlow(AiAdvisorUiState())
    val uiState = _uiState.asStateFlow()

    private suspend fun buildFinancialContextString(): String {
        val transactions = repository.allTransactions.first()
        val accounts = repository.allAccounts.first()
        val debts = repository.allDebts.first()
        val recurring = repository.allRecurring.first()
        val investments = repository.allInvestments.first()
        val budgets = repository.allBudgets.first()
        val goals = repository.allGoals.first()

        val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val totalExpenses = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val netBalance = totalIncome - totalExpenses

        val accountsStr = accounts.joinToString(", ") { "${it.name} (${it.type.name}): $${it.balance}" }
        val debtsStr = debts.joinToString("; ") { "${it.personName} (${it.type.name}): Nominal $${it.amount}, Dibayar $${it.paidAmount}, Lunas: ${it.isSettled}" }
        val recurringStr = recurring.joinToString("; ") { "${it.title}: $${it.amount} (${it.frequency.name})" }
        val investmentsStr = investments.joinToString("; ") { "${it.symbol} (${it.assetName}): Qty ${it.quantity}, Beli $${it.buyPrice}, Sekarang $${it.currentPrice}, Return: $${it.totalReturn}" }
        val budgetsStr = budgets.joinToString("; ") { "${it.category.name}: Limit $${it.limitAmount}, Terpakai $${it.spentAmount}" }
        val goalsStr = goals.joinToString("; ") { "${it.name}: $${it.currentAmount} / $${it.targetAmount}" }

        return """
            - Total Net Balance: $${netBalance}
            - Total Income: $${totalIncome}
            - Total Expenses: $${totalExpenses}
            - Accounts: $accountsStr
            - Debts & Receivables: $debtsStr
            - Recurring Obligations: $recurringStr
            - Investment Portfolio: $investmentsStr
            - Budgets: $budgetsStr
            - Savings Goals: $goalsStr
        """.trimIndent()
    }

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

    fun askQuestion(userQuestion: String) {
        if (userQuestion.isBlank()) return
        viewModelScope.launch {
            val userMsg = ChatMessage(isUser = true, text = userQuestion.trim())
            val updatedMessages = _uiState.value.chatMessages + userMsg
            _uiState.value = _uiState.value.copy(
                chatMessages = updatedMessages,
                isChatLoading = true,
                errorMessage = null
            )

            val contextStr = buildFinancialContextString()
            val aiResponse = geminiService.askFinancialQuestion(userQuestion, contextStr)

            val aiMsg = ChatMessage(isUser = false, text = aiResponse)
            _uiState.value = _uiState.value.copy(
                chatMessages = _uiState.value.chatMessages + aiMsg,
                isChatLoading = false
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
                    errorMessage = "Could not parse receipt image. Please verify API key in Secrets panel."
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
