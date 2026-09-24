package com.example.data.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object BiometricHelper {

    fun isBiometricAvailable(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        val canAuth = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
        )
        return canAuth == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun canAuthenticate(context: Context): Boolean {
        return isBiometricAvailable(context)
    }

    fun authenticateWithBiometrics(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (Int, CharSequence) -> Unit = { _, _ -> }
    ) {
        showBiometricPrompt(
            activity = activity,
            title = "Autentikasi Kai Finance",
            subtitle = "Gunakan sensor sidik jari atau wajah",
            negativeButtonText = "Gunakan PIN",
            onSuccess = onSuccess,
            onFallbackPin = { onError(BiometricPrompt.ERROR_NEGATIVE_BUTTON, "Batal") },
            onError = { err -> onError(-1, err) }
        )
    }

    fun showBiometricPrompt(
        activity: FragmentActivity,
        title: String = "Autentikasi Kai Finance",
        subtitle: String = "Gunakan sensor sidik jari atau wajah",
        negativeButtonText: String = "Gunakan PIN",
        onSuccess: () -> Unit,
        onFallbackPin: () -> Unit,
        onError: (String) -> Unit = {}
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON || errorCode == BiometricPrompt.ERROR_USER_CANCELED) {
                    onFallbackPin()
                } else {
                    onError(errString.toString())
                    onFallbackPin()
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onError("Sidik jari tidak cocok")
            }
        })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(negativeButtonText)
            .build()

        prompt.authenticate(promptInfo)
    }
}
