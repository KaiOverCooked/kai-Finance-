package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.KaiDatabase
import com.example.data.local.entity.DebtEntity
import com.example.data.local.entity.DebtPaymentEntity
import com.example.data.local.entity.DebtType
import com.example.data.repository.FinanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DebtsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = FinanceRepository(KaiDatabase.getDatabase(application))

    val debts: StateFlow<List<DebtEntity>> = repository.allDebts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedTab = MutableStateFlow(DebtType.PIUTANG)

    fun getPayments(debtId: Long): Flow<List<DebtPaymentEntity>> = repository.getPaymentsForDebt(debtId)

    fun createDebt(
        personName: String,
        amount: Double,
        type: DebtType,
        dueDateMillis: Long,
        notes: String
    ) {
        viewModelScope.launch {
            repository.addDebt(
                DebtEntity(
                    personName = personName,
                    amount = amount,
                    type = type,
                    dueDateMillis = dueDateMillis,
                    notes = notes
                )
            )
        }
    }

    fun recordPayment(debtId: Long, paymentAmount: Double, note: String) {
        viewModelScope.launch {
            repository.recordDebtPayment(debtId, paymentAmount, note)
        }
    }

    fun deleteDebt(id: Long) {
        viewModelScope.launch {
            repository.deleteDebtById(id)
        }
    }
}
