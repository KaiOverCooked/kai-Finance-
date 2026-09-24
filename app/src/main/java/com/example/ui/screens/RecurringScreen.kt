package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.RecurringCategoryType
import com.example.data.local.entity.RecurringEntity
import com.example.data.local.entity.RecurringFrequency
import com.example.data.local.entity.TransactionCategory
import com.example.data.local.entity.TransactionType
import com.example.ui.components.GlassCard
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun RecurringScreen(
    recurringList: List<RecurringEntity>,
    accounts: List<AccountEntity> = emptyList(),
    currencySymbol: String,
    onCreateRecurring: (String, Double, TransactionType, TransactionCategory, RecurringCategoryType, RecurringFrequency, Long, Boolean, String, Long?) -> Unit,
    onExecuteNow: (Long) -> Unit,
    onDeleteRecurring: (Long) -> Unit,
    onTriggerPendingCheck: () -> Unit = {},
    onBack: (() -> Unit)? = null
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var executeSuccessMessage by remember { mutableStateOf<String?>(null) }

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    val totalMonthlyCommitment = recurringList.filter { it.type == TransactionType.EXPENSE && it.isActive }.sumOf { it.amount }
    val totalRecurringIncome = recurringList.filter { it.type == TransactionType.INCOME && it.isActive }.sumOf { it.amount }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("recurring_screen")
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
                        text = "RECURRING / RUTIN",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Gaji, Uang Bulanan, Tagihan & Subscription",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.45f)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = {
                        onTriggerPendingCheck()
                        executeSuccessMessage = "Jadwal rutin yang jatuh tempo telah diperiksa & diproses otomatis!"
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(imageVector = Icons.Default.Autorenew, contentDescription = "Sync Recurring", tint = Color.White.copy(alpha = 0.7f))
                }

                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Jadwal", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Commitment Overview Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GlassCard(
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Pemasukan Rutin", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "+$currencySymbol ${String.format(Locale.US, "%,.0f", totalRecurringIncome)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = IncomeGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            GlassCard(
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Komitmen Rutin", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "-$currencySymbol ${String.format(Locale.US, "%,.0f", totalMonthlyCommitment)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = ExpenseRed,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Recurring Items List
        if (recurringList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Repeat,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Belum ada transaksi rutin terjadwal", color = Color.White.copy(alpha = 0.4f), fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(recurringList, key = { it.id }) { item ->
                    val connectedAccount = accounts.find { it.id == item.accountId }

                    GlassCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color.White.copy(alpha = 0.08f),
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Schedule,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color.White.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = item.recurringCategory.name,
                                                fontSize = 9.sp,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color.White.copy(alpha = 0.08f)
                                        ) {
                                            Text(
                                                text = item.frequency.name,
                                                fontSize = 9.sp,
                                                color = Color.White.copy(alpha = 0.7f),
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }

                                        if (item.autoExecute) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = Color.White
                                            ) {
                                                Text(
                                                    text = "AUTO",
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.Black,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    // Connected Account display
                                    if (connectedAccount != null) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(imageVector = Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color.White.copy(alpha = 0.5f), modifier = Modifier.size(10.dp))
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "${connectedAccount.name} (${connectedAccount.type.name})",
                                                fontSize = 10.sp,
                                                color = Color.White.copy(alpha = 0.5f)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Jatuh Tempo: ${dateFormat.format(Date(item.nextDueDateMillis))}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White.copy(alpha = 0.45f)
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${if (item.type == TransactionType.INCOME) "+" else "-"}$currencySymbol ${String.format(Locale.US, "%,.0f", item.amount)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (item.type == TransactionType.INCOME) IncomeGreen else Color.White,
                                    fontWeight = FontWeight.Bold
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Button(
                                        onClick = {
                                            onExecuteNow(item.id)
                                            executeSuccessMessage = "Telah dieksekusi dan dicatat ke transaksi serta saldo rekening!"
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text("Eksekusi", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }

                                    IconButton(
                                        onClick = { onDeleteRecurring(item.id) },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color.White.copy(alpha = 0.3f), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Recurring Dialog with Account Connection & Frequency Selection
    if (showAddDialog) {
        var title by remember { mutableStateOf("") }
        var amountStr by remember { mutableStateOf("") }
        var type by remember { mutableStateOf(TransactionType.EXPENSE) }
        var recCategory by remember { mutableStateOf(RecurringCategoryType.SUBSCRIPTION) }
        var frequency by remember { mutableStateOf(RecurringFrequency.MONTHLY) }
        var autoExecute by remember { mutableStateOf(true) }
        var selectedAccountId by remember { mutableStateOf<Long?>(accounts.firstOrNull()?.id) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Tambah Transaksi Rutin", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { type = TransactionType.EXPENSE },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (type == TransactionType.EXPENSE) Color.White else Color(0xFF1F1F1F),
                                contentColor = if (type == TransactionType.EXPENSE) Color.Black else Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Pengeluaran", fontSize = 11.sp)
                        }
                        Button(
                            onClick = { type = TransactionType.INCOME },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (type == TransactionType.INCOME) Color.White else Color(0xFF1F1F1F),
                                contentColor = if (type == TransactionType.INCOME) Color.Black else Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Pemasukan", fontSize = 11.sp)
                        }
                    }

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Nama (e.g. Netflix, Gaji, Listrik PLN)") },
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
                        label = { Text("Nominal") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    // Frequency selector (Daily, Weekly, Monthly, Yearly)
                    Text("Frekuensi Waktu:", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        RecurringFrequency.values().forEach { freq ->
                            Button(
                                onClick = { frequency = freq },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (frequency == freq) Color.White else Color(0xFF1F1F1F),
                                    contentColor = if (frequency == freq) Color.Black else Color.White
                                ),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 2.dp, vertical = 4.dp)
                            ) {
                                Text(freq.name.take(4), fontSize = 10.sp)
                            }
                        }
                    }

                    // Hubungkan Recurring -> Account
                    if (accounts.isNotEmpty()) {
                        Text("Hubungkan ke Rekening / Akun:", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(accounts) { acc ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (selectedAccountId == acc.id) Color.White else Color(0xFF1F1F1F),
                                    border = BorderStroke(1.dp, if (selectedAccountId == acc.id) Color.White else Color.White.copy(alpha = 0.15f)),
                                    modifier = Modifier.clickable { selectedAccountId = acc.id }
                                ) {
                                    Text(
                                        text = acc.name,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (selectedAccountId == acc.id) Color.Black else Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    Text("Kategori Rutin:", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        RecurringCategoryType.values().take(4).forEach { cat ->
                            Button(
                                onClick = { recCategory = cat },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (recCategory == cat) Color.White else Color(0xFF1F1F1F),
                                    contentColor = if (recCategory == cat) Color.Black else Color.White
                                ),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 2.dp, vertical = 4.dp)
                            ) {
                                Text(cat.name.take(6), fontSize = 10.sp)
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Otomatis Buat Transaksi", color = Color.White, fontSize = 13.sp)
                        Switch(
                            checked = autoExecute,
                            onCheckedChange = { autoExecute = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = Color.White)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = amountStr.toDoubleOrNull() ?: 0.0
                        if (title.isNotBlank() && amt > 0) {
                            // Calculate accurate next due date based on chosen frequency
                            val cal = Calendar.getInstance()
                            when (frequency) {
                                RecurringFrequency.DAILY -> cal.add(Calendar.DAY_OF_YEAR, 1)
                                RecurringFrequency.WEEKLY -> cal.add(Calendar.DAY_OF_YEAR, 7)
                                RecurringFrequency.MONTHLY -> cal.add(Calendar.MONTH, 1)
                                RecurringFrequency.YEARLY -> cal.add(Calendar.YEAR, 1)
                            }

                            onCreateRecurring(
                                title,
                                amt,
                                type,
                                if (type == TransactionType.INCOME) TransactionCategory.SALARY else TransactionCategory.UTILITIES,
                                recCategory,
                                frequency,
                                cal.timeInMillis,
                                autoExecute,
                                "",
                                selectedAccountId
                            )
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

    if (executeSuccessMessage != null) {
        AlertDialog(
            onDismissRequest = { executeSuccessMessage = null },
            title = { Text("Status Eksekusi", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text(executeSuccessMessage!!, color = Color.White.copy(alpha = 0.8f)) },
            confirmButton = {
                Button(
                    onClick = { executeSuccessMessage = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                ) {
                    Text("OK")
                }
            },
            containerColor = Color(0xFF141414)
        )
    }
}
