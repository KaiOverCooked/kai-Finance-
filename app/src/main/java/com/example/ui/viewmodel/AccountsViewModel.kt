package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.KaiDatabase
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.AccountType
import com.example.data.repository.FinanceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AccountsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = FinanceRepository(KaiDatabase.getDatabase(application))

    val accounts: StateFlow<List<AccountEntity>> = repository.allAccounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createAccount(name: String, type: AccountType, initialBalance: Double, accountNumber: String = "", institution: String = "") {
        viewModelScope.launch {
            repository.addAccount(
                AccountEntity(
                    name = name,
                    type = type,
                    balance = initialBalance,
                    accountNumber = accountNumber,
                    institutionName = institution
                )
            )
        }
    }

    fun transferBetweenAccounts(fromId: Long, toId: Long, amount: Double, note: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.transferBetweenAccounts(fromId, toId, amount, note)
            onResult(success)
        }
    }

    fun deleteAccount(id: Long) {
        viewModelScope.launch {
            repository.deleteAccountById(id)
        }
    }
}
