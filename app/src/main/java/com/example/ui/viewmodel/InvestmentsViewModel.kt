package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.KaiDatabase
import com.example.data.local.entity.InvestmentEntity
import com.example.data.local.entity.InvestmentType
import com.example.data.repository.FinanceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InvestmentsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = FinanceRepository(KaiDatabase.getDatabase(application))

    val investments: StateFlow<List<InvestmentEntity>> = repository.allInvestments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createInvestment(
        assetName: String,
        symbol: String,
        quantity: Double,
        buyPrice: Double,
        currentPrice: Double,
        type: InvestmentType,
        notes: String
    ) {
        viewModelScope.launch {
            repository.addInvestment(
                InvestmentEntity(
                    assetName = assetName,
                    symbol = symbol.uppercase(),
                    quantity = quantity,
                    buyPrice = buyPrice,
                    currentPrice = currentPrice,
                    type = type,
                    notes = notes
                )
            )
        }
    }

    fun updatePrice(id: Long, newPrice: Double) {
        viewModelScope.launch {
            repository.updateInvestmentPrice(id, newPrice)
        }
    }

    fun addDividend(id: Long, dividendAmount: Double) {
        viewModelScope.launch {
            repository.recordDividend(id, dividendAmount)
        }
    }

    fun deleteInvestment(id: Long) {
        viewModelScope.launch {
            repository.deleteInvestmentById(id)
        }
    }
}
