package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.integrity.DataIntegrityReport
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.security.BiometricHelper
import com.example.ui.components.CsvImportPreviewDialog
import com.example.ui.components.DataIntegrityDialog
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
    isBiometricEnabled: Boolean = false,
    onSetBiometricEnabled: (Boolean) -> Unit = {},
    currencySymbol: String,
    onCurrencyChange: (String) -> Unit,
    accounts: List<AccountEntity> = emptyList(),
    onExportCsv: suspend () -> String,
    onImportValidatedTransactions: suspend (List<TransactionEntity>) -> Int,
    onExportFullBackup: suspend () -> String,
    onRestoreFullBackup: suspend (String) -> Boolean,
    onRunDataIntegrityAudit: suspend () -> DataIntegrityReport,
    onReconcileAll: suspend () -> Int
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    var showPinSheet by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    var showCurrencyDropdown by remember { mutableStateOf(false) }
    var showSafeCsvImportDialog by remember { mutableStateOf(false) }
    var showRestoreBackupDialog by remember { mutableStateOf(false) }
    var rawTextImport by remember { mutableStateOf("") }

    // Data Integrity State
    var showIntegrityDialog by remember { mutableStateOf(false) }
    var integrityReport by remember { mutableStateOf<DataIntegrityReport?>(null) }
    var isAuditing by remember { mutableStateOf(false) }

    val canHardwareBiometric = remember(context) { BiometricHelper.isBiometricAvailable(context) }
    val currencies = listOf("Rp", "$", "€", "£", "¥", "₹")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("settings_screen")
    ) {
        // Header
        Text(
            text = "Pengaturan",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Keamanan, Kustomisasi, dan Audit Data",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.5f),
            modifier = Modifier.padding(top = 2.dp, bottom = 20.dp)
        )

        // Theme Customization
        GlassCard(
            modifier = Modifier.padding(bottom = 12.dp),
            testTag = "theme_setting_card"
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.DarkMode, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Tema Tampilan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Pilih tema warna aplikasi",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.45f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onThemeChange(AppThemeMode.DARK) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentThemeMode == AppThemeMode.DARK) Color.White else Color(0xFF1F1F1F),
                            contentColor = if (currentThemeMode == AppThemeMode.DARK) Color.Black else Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.DarkMode, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Dark Mode", fontSize = 12.sp)
                    }

                    Button(
                        onClick = { onThemeChange(AppThemeMode.LIGHT) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentThemeMode == AppThemeMode.LIGHT) Color.White else Color(0xFF1F1F1F),
                            contentColor = if (currentThemeMode == AppThemeMode.LIGHT) Color.Black else Color.White
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.LightMode, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Light Mode", fontSize = 12.sp)
                    }
                }
            }
        }

        // Security: PIN Salted Hashing & Biometric
        GlassCard(
            modifier = Modifier.padding(bottom = 12.dp),
            testTag = "security_setting_card"
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // PIN Lock
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "PIN Lock (Salted SHA-256)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (isPinEnabled) "Proteksi PIN aktif (Tersimpan aman terenkripsi)" else "Kunci aplikasi saat dibuka",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.45f)
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
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = Color.White),
                        modifier = Modifier.testTag("pin_lock_switch")
                    )
                }

                // Biometric Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(imageVector = Icons.Default.Fingerprint, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Autentikasi Biometrik",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (canHardwareBiometric) "Fingerprint / Face Unlock sistem" else "Perangkat belum memiliki biometrik aktif",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.45f)
                            )
                        }
                    }

                    Switch(
                        checked = isBiometricEnabled,
                        onCheckedChange = { onSetBiometricEnabled(it) },
                        enabled = canHardwareBiometric,
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = Color.White)
                    )
                }
            }
        }

        // Data Integrity & Balance Audit Card
        GlassCard(
            modifier = Modifier.padding(bottom = 12.dp),
            testTag = "data_integrity_card"
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Pemeriksaan Integritas Data",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Deteksi otomatis selisih saldo vs riwayat mutasi transaksi",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.45f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        isAuditing = true
                        showIntegrityDialog = true
                        coroutineScope.launch {
                            integrityReport = onRunDataIntegrityAudit()
                            isAuditing = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("run_integrity_check_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E22), contentColor = Color.White),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Jalankan Audit Saldo & Data", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
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
                    Icon(imageVector = Icons.Default.AttachMoney, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Mata Uang Utama",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Simbol penulisan nominal ($currencySymbol)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.45f)
                        )
                    }
                }

                Box {
                    Button(
                        onClick = { showCurrencyDropdown = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
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

        // CSV & Backup / Restore Module
        GlassCard(
            modifier = Modifier.padding(bottom = 12.dp),
            testTag = "backup_restore_card"
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.TableChart, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Backup, Restore & CSV",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Semua 9 Entitas: Transaksi, Akun, Hutang, Rutin, Budget, Investasi",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.45f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // CSV Buttons
                Text("CSV SPREADSHEET (VALIDASI AMAN):", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val csv = onExportCsv()
                                clipboardManager.setText(AnnotatedString(csv))
                                Toast.makeText(context, "CSV disalin ke Clipboard (${csv.lines().size} baris)", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier.weight(1f).height(42.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F1F1F), contentColor = Color.White),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export CSV", fontSize = 11.sp)
                    }

                    Button(
                        onClick = { showSafeCsvImportDialog = true },
                        modifier = Modifier.weight(1f).height(42.dp).testTag("import_csv_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F1F1F), contentColor = Color.White),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Upload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Import CSV", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Full Database Backup & Restore
                Text("CADANGAN LENGKAP 9 ENTITAS (JSON):", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val json = onExportFullBackup()
                                clipboardManager.setText(AnnotatedString(json))
                                Toast.makeText(context, "Cadangan Database lengkap disalin (${json.length} bytes)", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier.weight(1f).height(42.dp).testTag("backup_json_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Backup JSON", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            rawTextImport = ""
                            showRestoreBackupDialog = true
                        },
                        modifier = Modifier.weight(1f).height(42.dp).testTag("restore_json_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F1F1F), contentColor = Color.White),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Restore JSON", fontSize = 11.sp)
                    }
                }
            }
        }

        // About Card
        GlassCard(
            modifier = Modifier.padding(bottom = 12.dp),
            testTag = "about_card"
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Kai Finance Pro v2.5",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Room DB v4 • Exact Alarm Scheduler • Salted SHA-256 PIN • Data Integrity Checker",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.45f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // BottomSheet for PIN Setup
        if (showPinSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showPinSheet = false
                    pinInput = ""
                },
                sheetState = rememberModalBottomSheetState(),
                containerColor = Color(0xFF141414)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Atur PIN 4-Digit Baru",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "PIN akan di-hash secara aman dengan Salted SHA-256.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                    )

                    // Dots
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 0 until 4) {
                            val isFilled = i < pinInput.length
                            Surface(
                                shape = CircleShape,
                                color = if (isFilled) Color.White else Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.size(16.dp)
                            ) {}
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    val keys = listOf(
                        listOf("1", "2", "3"),
                        listOf("4", "5", "6"),
                        listOf("7", "8", "9"),
                        listOf("C", "0", "⌫")
                    )

                    keys.forEach { row ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            row.forEach { key ->
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF1F1F1F),
                                    modifier = Modifier.size(60.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            when (key) {
                                                "⌫" -> if (pinInput.isNotEmpty()) pinInput = pinInput.dropLast(1)
                                                "C" -> pinInput = ""
                                                else -> if (pinInput.length < 4) pinInput += key
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.Transparent,
                                            contentColor = Color.White
                                        ),
                                        shape = CircleShape
                                    ) {
                                        Text(text = key, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (pinInput.length == 4) {
                                onSetPin(pinInput, true)
                                showPinSheet = false
                                Toast.makeText(context, "PIN berhasil disimpan dan diaktifkan", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "PIN harus 4 digit", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_pin_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Simpan & Aktifkan PIN", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // Safe CSV Import Preview & Confirmation Dialog
        if (showSafeCsvImportDialog) {
            CsvImportPreviewDialog(
                accounts = accounts,
                onDismiss = { showSafeCsvImportDialog = false },
                onConfirmImport = { transactions, count ->
                    coroutineScope.launch {
                        val imported = onImportValidatedTransactions(transactions)
                        Toast.makeText(context, "Berhasil mengimpor $imported transaksi valid!", Toast.LENGTH_LONG).show()
                        showSafeCsvImportDialog = false
                    }
                }
            )
        }

        // Data Integrity Audit Dialog
        if (showIntegrityDialog) {
            DataIntegrityDialog(
                report = integrityReport,
                isLoading = isAuditing,
                onDismiss = { showIntegrityDialog = false },
                onRefreshAudit = {
                    isAuditing = true
                    coroutineScope.launch {
                        integrityReport = onRunDataIntegrityAudit()
                        isAuditing = false
                    }
                },
                onReconcileAll = {
                    isAuditing = true
                    coroutineScope.launch {
                        val fixed = onReconcileAll()
                        Toast.makeText(context, "Berhasil menyinkronkan $fixed ketidaksesuaian saldo!", Toast.LENGTH_LONG).show()
                        integrityReport = onRunDataIntegrityAudit()
                        isAuditing = false
                    }
                }
            )
        }

        // Restore Backup Dialog
        if (showRestoreBackupDialog) {
            AlertDialog(
                onDismissRequest = { showRestoreBackupDialog = false },
                title = { Text("Restore Database Penuh 9 Entitas", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            "Paste isi JSON cadangan database (Format 9 Entitas: Transaksi, Akun, Hutang, Pembayaran, Rutin, Budget, Goals, Investasi, Notifikasi):",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                        OutlinedTextField(
                            value = rawTextImport,
                            onValueChange = { rawTextImport = it },
                            placeholder = { Text("{\"transactions\": [...], \"accounts\": [...]}") },
                            modifier = Modifier.fillMaxWidth().height(140.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.White,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                val ok = onRestoreFullBackup(rawTextImport)
                                if (ok) {
                                    Toast.makeText(context, "Database 9 entitas berhasil dipulihkan secara menyeluruh!", Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, "Gagal memulihkan database. Format tidak valid.", Toast.LENGTH_LONG).show()
                                }
                                showRestoreBackupDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                    ) {
                        Text("Restore")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRestoreBackupDialog = false }) {
                        Text("Batal", color = Color.White.copy(alpha = 0.6f))
                    }
                },
                containerColor = Color(0xFF141414)
            )
        }
    }
}
