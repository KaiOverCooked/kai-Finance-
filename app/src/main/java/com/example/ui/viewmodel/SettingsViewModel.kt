package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.integrity.DataIntegrityReport
import com.example.data.local.KaiDatabase
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.preference.PreferencesRepository
import com.example.data.repository.FinanceRepository
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesRepository = PreferencesRepository(application)
    private val repository = FinanceRepository(KaiDatabase.getDatabase(application))

    val themeMode: StateFlow<AppThemeMode> = preferencesRepository.themeModeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppThemeMode.DARK)

    val isPinEnabled: StateFlow<Boolean> = preferencesRepository.isPinEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isBiometricEnabled: StateFlow<Boolean> = preferencesRepository.isBiometricEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val currencySymbol: StateFlow<String> = preferencesRepository.currencySymbolFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Rp")

    val accounts: StateFlow<List<AccountEntity>> = repository.allAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setThemeMode(mode: AppThemeMode) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(mode)
        }
    }

    fun setPin(pin: String, enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setPin(pin, enabled)
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setBiometricEnabled(enabled)
        }
    }

    fun setCurrency(symbol: String) {
        viewModelScope.launch {
            preferencesRepository.setCurrencySymbol(symbol)
        }
    }

    suspend fun exportCsv(): String {
        return repository.exportTransactionsCsv()
    }

    suspend fun importValidatedTransactions(transactions: List<TransactionEntity>): Int {
        return repository.importValidatedCsvTransactions(transactions)
    }

    suspend fun exportFullBackup(): String {
        return repository.exportFullBackupJson()
    }

    suspend fun restoreFullBackup(jsonString: String): Boolean {
        return repository.restoreFullBackupJson(jsonString)
    }

    suspend fun runDataIntegrityAudit(): DataIntegrityReport {
        return repository.runDataIntegrityCheck()
    }

    suspend fun reconcileAll(): Int {
        return repository.reconcileAllIntegrityIssues()
    }
}
