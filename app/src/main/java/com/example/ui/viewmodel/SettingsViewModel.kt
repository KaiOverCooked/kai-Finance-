package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.KaiDatabase
import com.example.data.preference.PreferencesRepository
import com.example.data.repository.FinanceRepository
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesRepository = PreferencesRepository(application)
    private val repository = FinanceRepository(KaiDatabase.getDatabase(application))

    val themeMode: StateFlow<AppThemeMode> = preferencesRepository.themeModeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppThemeMode.DARK)

    val isPinEnabled: StateFlow<Boolean> = preferencesRepository.isPinEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val currencySymbol: StateFlow<String> = preferencesRepository.currencySymbolFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "$")

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

    fun setCurrency(symbol: String) {
        viewModelScope.launch {
            preferencesRepository.setCurrencySymbol(symbol)
        }
    }

    suspend fun exportDataJson(): String {
        val transactions = repository.allTransactions.first()
        val jsonArray = JSONArray()
        transactions.forEach { tx ->
            val obj = JSONObject()
            obj.put("title", tx.title)
            obj.put("amount", tx.amount)
            obj.put("type", tx.type.name)
            obj.put("category", tx.category.name)
            obj.put("timestamp", tx.timestamp)
            obj.put("note", tx.note)
            jsonArray.put(obj)
        }
        val exportObj = JSONObject()
        exportObj.put("app", "Kai Finance")
        exportObj.put("version", "1.0")
        exportObj.put("transactions", jsonArray)
        return exportObj.toString(2)
    }
}
