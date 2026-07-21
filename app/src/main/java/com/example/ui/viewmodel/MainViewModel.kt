package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.KaiDatabase
import com.example.data.preference.PreferencesRepository
import com.example.data.repository.FinanceRepository
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
            repository.seedInitialDataIfEmpty()
        }
    }

    val themeMode: StateFlow<AppThemeMode> = preferencesRepository.themeModeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppThemeMode.DARK)

    val isPinEnabled: StateFlow<Boolean> = preferencesRepository.isPinEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val userPin: StateFlow<String> = preferencesRepository.userPinFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val currencySymbol: StateFlow<String> = preferencesRepository.currencySymbolFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "$")

    private val _isUnlocked = MutableStateFlow(true)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    fun unlockWithPin(pinInput: String): Boolean {
        if (pinInput == userPin.value) {
            _isUnlocked.value = true
            return true
        }
        return false
    }

    fun lockApp() {
        if (isPinEnabled.value && userPin.value.isNotEmpty()) {
            _isUnlocked.value = false
        }
    }
}
