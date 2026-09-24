package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.data.security.BiometricHelper
import kotlinx.coroutines.launch

@Composable
fun PinLockScreen(
    isBiometricEnabled: Boolean = false,
    onUnlock: suspend (String) -> Boolean,
    onBiometricUnlock: () -> Unit
) {
    var pinInput by remember { mutableStateOf("") }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val canBiometric = remember(context) { BiometricHelper.isBiometricAvailable(context) }

    fun triggerBiometric() {
        val activity = context as? FragmentActivity
        if (activity != null && canBiometric) {
            BiometricHelper.authenticateWithBiometrics(
                activity = activity,
                onSuccess = { onBiometricUnlock() },
                onError = { _: Int, _: CharSequence -> /* Fallback to PIN seamlessly */ }
            )
        }
    }

    LaunchedEffect(Unit) {
        if (isBiometricEnabled && canBiometric) {
            triggerBiometric()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .testTag("pin_lock_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(72.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Lock",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "KAI FINANCE",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Masukkan PIN",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(32.dp))

        // PIN Indicator Dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            for (i in 0 until 4) {
                val isFilled = i < pinInput.length
                Surface(
                    shape = CircleShape,
                    color = if (isFilled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    modifier = Modifier.size(16.dp)
                ) {}
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Keypad Grid
        val keys = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("C", "0", "⌫")
        )

        keys.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { key ->
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .size(64.dp)
                            .testTag("pin_key_$key")
                    ) {
                        Button(
                            onClick = {
                                when (key) {
                                    "⌫" -> if (pinInput.isNotEmpty()) pinInput = pinInput.dropLast(1)
                                    "C" -> pinInput = ""
                                    else -> {
                                        if (pinInput.length < 4) {
                                            pinInput += key
                                            if (pinInput.length == 4) {
                                                coroutineScope.launch {
                                                    val success = onUnlock(pinInput)
                                                    if (!success) {
                                                        Toast.makeText(context, "PIN tidak cocok", Toast.LENGTH_SHORT).show()
                                                        pinInput = ""
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ),
                            shape = CircleShape
                        ) {
                            Text(
                                text = key,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        if (canBiometric) {
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedButton(
                onClick = { triggerBiometric() },
                shape = CircleShape,
                modifier = Modifier.testTag("biometric_login_btn")
            ) {
                Icon(Icons.Default.Fingerprint, contentDescription = "Biometrik", modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Buka dengan Biometrik", fontSize = 13.sp)
            }
        }
    }
}
