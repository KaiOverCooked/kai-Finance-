package com.example.data.security

import java.security.MessageDigest
import java.security.SecureRandom

object SecurityHelper {
    private const val PEPPER = "kai_finance_secure_pepper_v2_2026"

    fun generateSalt(): String {
        val random = SecureRandom()
        val saltBytes = ByteArray(16)
        random.nextBytes(saltBytes)
        return saltBytes.joinToString("") { "%02x".format(it) }
    }

    fun hashPin(pin: String, salt: String): String {
        val combined = "$salt:$pin:$PEPPER"
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(combined.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun verifyPin(inputPin: String, salt: String, expectedHash: String): Boolean {
        if (expectedHash.isBlank()) return false
        val computedHash = hashPin(inputPin, salt)
        // Constant-time comparison to prevent timing attacks
        return MessageDigest.isEqual(
            computedHash.toByteArray(Charsets.UTF_8),
            expectedHash.toByteArray(Charsets.UTF_8)
        )
    }
}
