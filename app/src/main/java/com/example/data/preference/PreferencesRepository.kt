package com.example.data.preference

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "kai_settings")

class PreferencesRepository(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val IS_PIN_ENABLED = booleanPreferencesKey("is_pin_enabled")
        val USER_PIN = stringPreferencesKey("user_pin")
        val IS_BIOMETRIC_ENABLED = booleanPreferencesKey("is_biometric_enabled")
        val CURRENCY_SYMBOL = stringPreferencesKey("currency_symbol")
    }

    val themeModeFlow: Flow<AppThemeMode> = context.dataStore.data.map { prefs ->
        val modeStr = prefs[Keys.THEME_MODE] ?: AppThemeMode.DARK.name
        try {
            AppThemeMode.valueOf(modeStr)
        } catch (e: Exception) {
            AppThemeMode.DARK
        }
    }

    val isPinEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.IS_PIN_ENABLED] ?: false
    }

    val userPinFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.USER_PIN] ?: ""
    }

    val isBiometricEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.IS_BIOMETRIC_ENABLED] ?: false
    }

    val currencySymbolFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.CURRENCY_SYMBOL] ?: "$"
    }

    suspend fun setThemeMode(mode: AppThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode.name
        }
    }

    suspend fun setPin(pin: String, enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.USER_PIN] = pin
            prefs[Keys.IS_PIN_ENABLED] = enabled
        }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_BIOMETRIC_ENABLED] = enabled
        }
    }

    suspend fun setCurrencySymbol(symbol: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.CURRENCY_SYMBOL] = symbol
        }
    }
}
