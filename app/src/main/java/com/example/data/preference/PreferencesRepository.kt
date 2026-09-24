package com.example.data.preference

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.security.SecurityHelper
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "kai_settings")

class PreferencesRepository(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val IS_PIN_ENABLED = booleanPreferencesKey("is_pin_enabled")
        val LEGACY_USER_PIN = stringPreferencesKey("user_pin")
        val PIN_SALT = stringPreferencesKey("pin_salt")
        val PIN_HASH = stringPreferencesKey("pin_hash")
        val IS_BIOMETRIC_ENABLED = booleanPreferencesKey("is_biometric_enabled")
        val CURRENCY_SYMBOL = stringPreferencesKey("currency_symbol")
        val IS_ONBOARDING_COMPLETED = booleanPreferencesKey("is_onboarding_completed")
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

    val hasPinConfiguredFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        val hasHash = !prefs[Keys.PIN_HASH].isNullOrBlank()
        val hasLegacy = !prefs[Keys.LEGACY_USER_PIN].isNullOrBlank()
        hasHash || hasLegacy
    }

    val isBiometricEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.IS_BIOMETRIC_ENABLED] ?: false
    }

    val currencySymbolFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.CURRENCY_SYMBOL] ?: "Rp"
    }

    val isOnboardingCompletedFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.IS_ONBOARDING_COMPLETED] ?: false
    }

    suspend fun setThemeMode(mode: AppThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode.name
        }
    }

    suspend fun setPin(pin: String, enabled: Boolean) {
        val salt = SecurityHelper.generateSalt()
        val hash = SecurityHelper.hashPin(pin, salt)
        context.dataStore.edit { prefs ->
            prefs[Keys.PIN_SALT] = salt
            prefs[Keys.PIN_HASH] = hash
            prefs[Keys.IS_PIN_ENABLED] = enabled
            prefs.remove(Keys.LEGACY_USER_PIN) // Ensure plaintext is completely purged
        }
    }

    suspend fun removePin() {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_PIN_ENABLED] = false
            prefs.remove(Keys.PIN_HASH)
            prefs.remove(Keys.PIN_SALT)
            prefs.remove(Keys.LEGACY_USER_PIN)
        }
    }

    suspend fun verifyPin(inputPin: String): Boolean {
        val prefs = context.dataStore.data.first()
        val salt = prefs[Keys.PIN_SALT]
        val hash = prefs[Keys.PIN_HASH]

        // 1. Check modern salted hash
        if (!salt.isNullOrBlank() && !hash.isNullOrBlank()) {
            return SecurityHelper.verifyPin(inputPin, salt, hash)
        }

        // 2. Transparent migration from legacy plaintext if it existed
        val legacy = prefs[Keys.LEGACY_USER_PIN]
        if (!legacy.isNullOrBlank()) {
            if (legacy == inputPin) {
                // Auto-upgrade to salted SHA-256
                setPin(inputPin, prefs[Keys.IS_PIN_ENABLED] ?: true)
                return true
            }
        }
        return false
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

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.IS_ONBOARDING_COMPLETED] = completed
        }
    }
}
