package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.GlassCard
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentThemeMode: AppThemeMode,
    onThemeChange: (AppThemeMode) -> Unit,
    isPinEnabled: Boolean,
    onSetPin: (String, Boolean) -> Unit,
    currencySymbol: String,
    onCurrencyChange: (String) -> Unit,
    onExportDataJson: suspend () -> String
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var showPinSheet by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    var showCurrencyDropdown by remember { mutableStateOf(false) }

    val currencies = listOf("$", "€", "£", "¥", "₹")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .testTag("settings_screen")
    ) {
        Text(
            text = "PREFERENCES",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "App Settings",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Appearance Theme
        GlassCard(
            modifier = Modifier.padding(bottom = 12.dp),
            testTag = "theme_setting_card"
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Palette, contentDescription = null)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Theme Mode",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppThemeMode.entries.forEach { mode ->
                        val isSelected = currentThemeMode == mode
                        Button(
                            onClick = { onThemeChange(mode) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("theme_btn_${mode.name}"),
                            shape = RoundedCornerShape(10.dp),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text(
                                text = mode.name.lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
        }

        // Security PIN Lock
        GlassCard(
            modifier = Modifier.padding(bottom = 12.dp),
            testTag = "security_setting_card"
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "PIN Lock Security",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isPinEnabled) "4-Digit PIN Protection Active" else "Protect application with passcode",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = isPinEnabled,
                    onCheckedChange = { checked ->
                        if (checked) {
                            showPinSheet = true
                        } else {
                            onSetPin("", false)
                        }
                    },
                    modifier = Modifier.testTag("pin_lock_switch")
                )
            }
        }

        // Currency Selector
        GlassCard(
            modifier = Modifier.padding(bottom = 12.dp),
            testTag = "currency_setting_card"
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.AttachMoney, contentDescription = null)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Primary Currency",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                androidx.compose.foundation.layout.Box {
                    Button(
                        onClick = { showCurrencyDropdown = true },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("currency_selector_btn")
                    ) {
                        Text(currencySymbol, fontWeight = FontWeight.Bold)
                    }

                    DropdownMenu(
                        expanded = showCurrencyDropdown,
                        onDismissRequest = { showCurrencyDropdown = false }
                    ) {
                        currencies.forEach { curr ->
                            DropdownMenuItem(
                                text = { Text(curr) },
                                onClick = {
                                    onCurrencyChange(curr)
                                    showCurrencyDropdown = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Data Export
        GlassCard(
            modifier = Modifier.padding(bottom = 12.dp),
            testTag = "export_data_card"
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Backup & Export",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Export transactions as JSON file",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = {
                        coroutineScope.launch {
                            val jsonStr = onExportDataJson()
                            Toast.makeText(context, "Exported ${jsonStr.length} bytes to local backup.", Toast.LENGTH_LONG).show()
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("export_json_btn")
                ) {
                    Text("Export")
                }
            }
        }

        // About Card
        GlassCard(
            modifier = Modifier.padding(bottom = 12.dp),
            testTag = "about_card"
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Info, contentDescription = null)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Kai Finance v1.0",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Premium Monochrome Personal Finance Engine • Gemini Intelligence",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))

        if (showPinSheet) {
            val sheetState = rememberModalBottomSheetState()
            ModalBottomSheet(
                onDismissRequest = { showPinSheet = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .testTag("set_pin_sheet")
                ) {
                    Text(
                        text = "Set 4-Digit Passcode",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 4) pinInput = it },
                        label = { Text("Enter 4 digits") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_new_pin"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (pinInput.length == 4) {
                                onSetPin(pinInput, true)
                                showPinSheet = false
                            } else {
                                Toast.makeText(context, "PIN must be exactly 4 digits", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_pin_btn"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Enable PIN Lock", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
