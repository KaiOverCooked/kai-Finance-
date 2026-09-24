package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.integrity.DataIntegrityReport
import com.example.data.local.KaiDatabase
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.AccountType
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.TransactionCategory
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.TransactionType
import com.example.data.preference.PreferencesRepository
import com.example.data.repository.FinanceRepository
import com.example.data.scheduler.RecurringScheduler
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val repository: FinanceRepository
    val preferencesRepository: PreferencesRepository

    init {
        val db = KaiDatabase.getDatabase(application)
        repository = FinanceRepository(db)
        preferencesRepository = PreferencesRepository(application)

        viewModelScope.launch {
            // Seed initial sample data if empty and not onboarding
            val isCompleted = preferencesRepository.isOnboardingCompletedFlow.first()
            if (isCompleted) {
                repository.seedInitialDataIfEmpty()
            }
            // Check smart recurring alarms
            RecurringScheduler.scheduleNextExactAlarm(application)
        }
    }

    val themeMode: StateFlow<AppThemeMode> = preferencesRepository.themeModeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppThemeMode.DARK)

    val isPinEnabled: StateFlow<Boolean> = preferencesRepository.isPinEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isBiometricEnabled: StateFlow<Boolean> = preferencesRepository.isBiometricEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val currencySymbol: StateFlow<String> = preferencesRepository.currencySymbolFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Rp")

    val isOnboardingCompleted: StateFlow<Boolean> = preferencesRepository.isOnboardingCompletedFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    init {
        // If PIN is disabled, auto unlock
        viewModelScope.launch {
            preferencesRepository.isPinEnabledFlow.collect { enabled ->
                if (!enabled) {
                    _isUnlocked.value = true
                }
            }
        }
    }

    suspend fun unlockWithPin(pinInput: String): Boolean {
        val isValid = preferencesRepository.verifyPin(pinInput)
        if (isValid) {
            _isUnlocked.value = true
            return true
        }
        return false
    }

    fun unlockWithBiometric() {
        _isUnlocked.value = true
    }

    fun lockApp() {
        if (isPinEnabled.value) {
            _isUnlocked.value = false
        }
    }

    fun completeOnboarding(
        accountName: String,
        accountType: AccountType,
        initialBalance: Double,
        budgetCategory: TransactionCategory?,
        budgetLimit: Double?
    ) {
        viewModelScope.launch {
            // 1. Create the primary account
            val accountId = repository.addAccount(
                AccountEntity(
                    name = accountName,
                    type = accountType,
                    balance = initialBalance,
                    initialBalance = initialBalance,
                    isDefault = true
                )
            )

            // 2. Add starting balance transaction if initialBalance > 0
            if (initialBalance > 0) {
                repository.addTransaction(
                    TransactionEntity(
                        title = "Saldo Awal ($accountName)",
                        amount = initialBalance,
                        type = TransactionType.INCOME,
                        category = TransactionCategory.SALARY,
                        timestamp = System.currentTimeMillis(),
                        note = "Saldo awal saat setup akun pertama",
                        accountId = accountId
                    )
                )
            }

            // 3. Create budget if provided
            if (budgetCategory != null && budgetLimit != null && budgetLimit > 0) {
                repository.addBudget(
                    BudgetEntity(
                        category = budgetCategory,
                        limitAmount = budgetLimit,
                        spentAmount = 0.0,
                        period = "Monthly"
                    )
                )
            }

            // 4. Mark onboarding completed
            preferencesRepository.setOnboardingCompleted(true)
            _isUnlocked.value = true
        }
    }

    suspend fun runDataIntegrityCheck(): DataIntegrityReport {
        return repository.runDataIntegrityCheck()
    }

    suspend fun reconcileAllIntegrityIssues(): Int {
        return repository.reconcileAllIntegrityIssues()
    }

    suspend fun importValidatedCsv(transactions: List<TransactionEntity>): Int {
        return repository.importValidatedCsvTransactions(transactions)
    }
}
