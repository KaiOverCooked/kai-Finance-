package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.TransactionType
import com.example.data.util.CsvValidator
import com.example.ui.util.CurrencyFormatter
import com.example.ui.util.CurrencyFormatter.formatRupiah

@Composable
fun CsvImportPreviewDialog(
    accounts: List<AccountEntity>,
    onDismiss: () -> Unit,
    onConfirmImport: (transactions: List<TransactionEntity>, count: Int) -> Unit
) {
    var rawText by remember { mutableStateOf("") }
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id) }
    var accountDropdownExpanded by remember { mutableStateOf(false) }
    var isReviewExpanded by remember { mutableStateOf(false) }

    val summary by remember(rawText) {
        derivedStateOf {
            if (rawText.isBlank()) null else CsvValidator.validateCsv(rawText)
        }
    }

    val selectedAccount = accounts.find { it.id == selectedAccountId }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Import Data CSV (Aman)",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Tutup",
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onDismiss() }
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Format yang didukung: Judul, Nominal, Tipe (INCOME/EXPENSE), Kategori, Tanggal (opsional), Catatan (opsional).",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                // Input Box
                OutlinedTextField(
                    value = rawText,
                    onValueChange = { rawText = it },
                    placeholder = {
                        Text(
                            "Gaji Kantor, 12000000, INCOME, SALARY\nKopi Kenangan, 28000, EXPENSE, FOOD\nSewa Apartemen, 3500000, EXPENSE, HOUSING",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.3f)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .testTag("csv_input_field"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                // Target Account Selector
                if (accounts.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "Akun Tujuan untuk Transaksi:",
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 11.sp
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF1C1C1E))
                                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                                .clickable { accountDropdownExpanded = true }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedAccount?.let { "${it.name} (${CurrencyFormatter.formatRupiah(it.balance)})" }
                                        ?: "Pilih Akun",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.White)
                            }

                            DropdownMenu(
                                expanded = accountDropdownExpanded,
                                onDismissRequest = { accountDropdownExpanded = false },
                                modifier = Modifier.background(Color(0xFF242426))
                            ) {
                                accounts.forEach { acc ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                "${acc.name} - ${CurrencyFormatter.formatRupiah(acc.balance)}",
                                                color = Color.White,
                                                fontSize = 13.sp
                                            )
                                        },
                                        onClick = {
                                            selectedAccountId = acc.id
                                            accountDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Validation Summary Report
                summary?.let { res ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1C1C1E))
                            .padding(12.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Hasil Validasi: ${res.totalFound} baris ditemukan",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 13.sp
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Valid Badge
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF1A3826))
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(14.dp))
                                    Text("✓ ${res.validCount} valid", color = Color(0xFF81C784), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                // Invalid Badge
                                if (res.invalidCount > 0) {
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF381F1A))
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFFF7043), modifier = Modifier.size(14.dp))
                                        Text("⚠ ${res.invalidCount} invalid", color = Color(0xFFFFAB91), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // Toggle Review Button
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { isReviewExpanded = !isReviewExpanded }
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isReviewExpanded) "Sembunyikan Review" else "Tinjau Data (${res.totalFound})",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Icon(
                                    if (isReviewExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.6f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Review List
                            AnimatedVisibility(visible = isReviewExpanded) {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 140.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    items(res.validRows) { item ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF141414))
                                                .padding(horizontal = 8.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(12.dp))
                                                Text(
                                                    "${item.title} (${item.category.name})",
                                                    color = Color.White,
                                                    fontSize = 11.sp,
                                                    maxLines = 1
                                                )
                                            }
                                            Text(
                                                (if (item.type == TransactionType.INCOME) "+" else "-") + CurrencyFormatter.formatRupiah(item.amount),
                                                color = if (item.type == TransactionType.INCOME) Color(0xFF4CAF50) else Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    items(res.invalidRows) { invalid ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color(0xFF281818))
                                                .padding(horizontal = 8.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFFF5252), modifier = Modifier.size(12.dp))
                                                Text(
                                                    "Baris #${invalid.lineNumber}: ${invalid.errorMessage}",
                                                    color = Color(0xFFFF8A80),
                                                    fontSize = 11.sp,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            val validCount = summary?.validCount ?: 0
            Button(
                onClick = {
                    summary?.let { res ->
                        val entities = res.toTransactionEntities(selectedAccountId)
                        onConfirmImport(entities, res.validCount)
                    }
                },
                enabled = validCount > 0,
                modifier = Modifier.testTag("confirm_import_csv_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black,
                    disabledContainerColor = Color.White.copy(alpha = 0.2f),
                    disabledContentColor = Color.White.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (validCount > 0) "Import $validCount Transaksi Valid" else "Import CSV",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = Color.White.copy(alpha = 0.6f))
            }
        },
        containerColor = Color(0xFF141414),
        shape = RoundedCornerShape(20.dp)
    )
}
