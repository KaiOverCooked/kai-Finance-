package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.DebtEntity
import com.example.data.local.entity.DebtPaymentEntity
import com.example.data.local.entity.DebtType
import com.example.ui.components.GlassCard
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DebtsScreen(
    debts: List<DebtEntity>,
    currencySymbol: String,
    getPayments: (Long) -> Flow<List<DebtPaymentEntity>>,
    onCreateDebt: (String, Double, DebtType, Long, String) -> Unit,
    onRecordPayment: (Long, Double, String) -> Unit,
    onDeleteDebt: (Long) -> Unit,
    onBack: (() -> Unit)? = null
) {
    var selectedType by remember { mutableStateOf(DebtType.PIUTANG) }
    var showAddDialog by remember { mutableStateOf(false) }
    var payingDebt by remember { mutableStateOf<DebtEntity?>(null) }
    var historyDebt by remember { mutableStateOf<DebtEntity?>(null) }

    val filteredDebts = debts.filter { it.type == selectedType }

    val totalHutang = debts.filter { it.type == DebtType.HUTANG && !it.isSettled }.sumOf { it.amount - it.paidAmount }
    val totalPiutang = debts.filter { it.type == DebtType.PIUTANG && !it.isSettled }.sumOf { it.amount - it.paidAmount }

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("debts_screen")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Column {
                    Text(
                        text = "HUTANG & PIUTANG",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Manajemen Pinjaman, Jatuh Tempo & Cicilan",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.45f)
                    )
                }
            }

            Surface(
                shape = CircleShape,
                color = Color.White,
                modifier = Modifier.size(42.dp)
            ) {
                IconButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier.testTag("add_debt_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Debt", tint = Color.Black)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Summary Hero Card
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            GlassCard(modifier = Modifier.weight(1f)) {
                Column {
                    Text("TOTAL PIUTANG", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                    Text("(Uang di Orang Lain)", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.3f), fontSize = 9.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "$currencySymbol${String.format(Locale.US, "%,.2f", totalPiutang)}",
                        style = MaterialTheme.typography.titleLarge,
                        color = IncomeGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            GlassCard(modifier = Modifier.weight(1f)) {
                Column {
                    Text("TOTAL HUTANG", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                    Text("(Kewajiban Anda)", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.3f), fontSize = 9.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "$currencySymbol${String.format(Locale.US, "%,.2f", totalHutang)}",
                        style = MaterialTheme.typography.titleLarge,
                        color = ExpenseRed,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Tabs
        TabRow(
            selectedTabIndex = if (selectedType == DebtType.PIUTANG) 0 else 1,
            containerColor = Color.Transparent,
            indicator = { tabPositions ->
                val index = if (selectedType == DebtType.PIUTANG) 0 else 1
                SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[index]),
                    color = Color.White
                )
            },
            divider = {}
        ) {
            Tab(
                selected = selectedType == DebtType.PIUTANG,
                onClick = { selectedType = DebtType.PIUTANG },
                text = {
                    Text(
                        "Piutang (Mereka Berhutang)",
                        fontWeight = if (selectedType == DebtType.PIUTANG) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedType == DebtType.PIUTANG) Color.White else Color.White.copy(alpha = 0.4f)
                    )
                }
            )
            Tab(
                selected = selectedType == DebtType.HUTANG,
                onClick = { selectedType = DebtType.HUTANG },
                text = {
                    Text(
                        "Hutang (Saya Berhutang)",
                        fontWeight = if (selectedType == DebtType.HUTANG) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedType == DebtType.HUTANG) Color.White else Color.White.copy(alpha = 0.4f)
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // List
        if (filteredDebts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Tidak ada data ${if (selectedType == DebtType.PIUTANG) "piutang" else "hutang"}", color = Color.White.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                    ) {
                        Text("Catat Baru")
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredDebts, key = { it.id }) { debt ->
                    val progress = if (debt.amount > 0) (debt.paidAmount / debt.amount).toFloat().coerceIn(0f, 1f) else 0f
                    val sisa = (debt.amount - debt.paidAmount).coerceAtLeast(0.0)
                    val isPastDue = System.currentTimeMillis() > debt.dueDateMillis && !debt.isSettled

                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = debt.personName,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Jatuh tempo: ${dateFormat.format(Date(debt.dueDateMillis))}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isPastDue) ExpenseRed else Color.White.copy(alpha = 0.45f)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (debt.isSettled) IncomeGreen.copy(alpha = 0.15f) else if (isPastDue) ExpenseRed.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.10f)
                                ) {
                                    Text(
                                        text = if (debt.isSettled) "LUNAS" else if (isPastDue) "TERLAMBAT" else "BELUM LUNAS",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (debt.isSettled) IncomeGreen else if (isPastDue) ExpenseRed else Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Nominal & Sisa
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Total: $currencySymbol${String.format(Locale.US, "%,.2f", debt.amount)}",
                                    fontSize = 13.sp,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = "Sisa: $currencySymbol${String.format(Locale.US, "%,.2f", sisa)}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (debt.isSettled) IncomeGreen else Color.White
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                color = if (debt.isSettled) IncomeGreen else Color.White,
                                trackColor = Color.White.copy(alpha = 0.12f)
                            )

                            if (debt.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Catatan: ${debt.notes}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.4f)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (!debt.isSettled) {
                                    Button(
                                        onClick = { payingDebt = debt },
                                        modifier = Modifier.weight(1f).height(40.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Catat Bayar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                Button(
                                    onClick = { historyDebt = debt },
                                    modifier = Modifier.height(40.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F1F1F), contentColor = Color.White),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Riwayat", fontSize = 12.sp)
                                }

                                IconButton(onClick = { onDeleteDebt(debt.id) }) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Debt Dialog
    if (showAddDialog) {
        var personName by remember { mutableStateOf("") }
        var amountStr by remember { mutableStateOf("") }
        var notes by remember { mutableStateOf("") }
        var debtType by remember { mutableStateOf(selectedType) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Catat Hutang / Piutang", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { debtType = DebtType.PIUTANG },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (debtType == DebtType.PIUTANG) Color.White else Color(0xFF1F1F1F),
                                contentColor = if (debtType == DebtType.PIUTANG) Color.Black else Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Piutang", fontSize = 12.sp)
                        }
                        Button(
                            onClick = { debtType = DebtType.HUTANG },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (debtType == DebtType.HUTANG) Color.White else Color(0xFF1F1F1F),
                                contentColor = if (debtType == DebtType.HUTANG) Color.Black else Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Hutang", fontSize = 12.sp)
                        }
                    }

                    OutlinedTextField(
                        value = personName,
                        onValueChange = { personName = it },
                        label = { Text("Siapa (Nama Orang / Pihak)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        label = { Text("Nominal Uang") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Catatan / Keperluan (Opsional)") },
                        modifier = Modifier.fillMaxWidth(),
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
                        val amt = amountStr.toDoubleOrNull() ?: 0.0
                        if (personName.isNotBlank() && amt > 0) {
                            val defaultDue = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000L
                            onCreateDebt(personName, amt, debtType, defaultDue, notes)
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Batal", color = Color.White.copy(alpha = 0.6f))
                }
            },
            containerColor = Color(0xFF141414)
        )
    }

    // Record Payment Dialog
    if (payingDebt != null) {
        val debt = payingDebt!!
        var payAmountStr by remember { mutableStateOf("") }
        var payNote by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { payingDebt = null },
            title = { Text("Catat Pembayaran: ${debt.personName}", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val sisa = debt.amount - debt.paidAmount
                    Text("Sisa tagihan saat ini: $currencySymbol${String.format(Locale.US, "%,.2f", sisa)}", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)

                    OutlinedTextField(
                        value = payAmountStr,
                        onValueChange = { payAmountStr = it },
                        label = { Text("Nominal Pembayaran") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    OutlinedTextField(
                        value = payNote,
                        onValueChange = { payNote = it },
                        label = { Text("Catatan Pembayaran (Opsional)") },
                        modifier = Modifier.fillMaxWidth(),
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
                        val amt = payAmountStr.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            onRecordPayment(debt.id, amt, payNote)
                            payingDebt = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                ) {
                    Text("Simpan Pembayaran")
                }
            },
            dismissButton = {
                TextButton(onClick = { payingDebt = null }) {
                    Text("Batal", color = Color.White.copy(alpha = 0.6f))
                }
            },
            containerColor = Color(0xFF141414)
        )
    }

    // Payment History Modal Dialog
    if (historyDebt != null) {
        val debt = historyDebt!!
        val payments by getPayments(debt.id).collectAsState(initial = emptyList())

        AlertDialog(
            onDismissRequest = { historyDebt = null },
            title = { Text("Riwayat Pembayaran: ${debt.personName}", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (payments.isEmpty()) {
                        Text("Belum ada riwayat pembayaran.", color = Color.White.copy(alpha = 0.5f))
                    } else {
                        payments.forEach { p ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(p.note.ifBlank { "Pembayaran" }, color = Color.White, fontSize = 13.sp)
                                    Text(dateFormat.format(Date(p.timestamp)), color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
                                }
                                Text(
                                    text = "$currencySymbol${String.format(Locale.US, "%,.2f", p.amount)}",
                                    color = IncomeGreen,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { historyDebt = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                ) {
                    Text("Tutup")
                }
            },
            containerColor = Color(0xFF141414)
        )
    }
}
