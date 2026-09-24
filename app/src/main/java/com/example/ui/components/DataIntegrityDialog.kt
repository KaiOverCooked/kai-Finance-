package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.integrity.DataIntegrityReport
import com.example.ui.util.CurrencyFormatter.formatRupiah
import com.example.ui.util.CurrencyFormatter

@Composable
fun DataIntegrityDialog(
    report: DataIntegrityReport?,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onRefreshAudit: () -> Unit,
    onReconcileAll: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        if (report?.isHealthy == true) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (report?.isHealthy == true) Color(0xFF4CAF50) else Color(0xFFFFB74D),
                        modifier = Modifier.size(22.dp)
                    )
                    Text("Pemeriksaan Integritas Data", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Tutup",
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp).clickable { onDismiss() }
                )
            }
        },
        text = {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else if (report != null) {
                Column(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 440.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Status Overview Card
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (report.isHealthy) Color(0xFF132A1C) else Color(0xFF2C1E12)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(40.dp).clip(CircleShape).background(
                                    if (report.isHealthy) Color(0xFF2E7D32) else Color(0xFFE65100)
                                ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "${report.healthyScorePercentage}%",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                            Column {
                                Text(
                                    if (report.isHealthy) "Semua Saldo & Data Konsisten" else "⚠️ Balance Mismatch Terdeteksi",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    if (report.isHealthy)
                                        "${report.totalAccountsChecked} akun dan ${report.totalTransactionsChecked} transaksi 100% selaras."
                                    else
                                        "Ditemukan selisih antara saldo akun dan riwayat mutasi transaksi.",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    // Account Balances Audit List
                    Text(
                        "Audit Saldo Akun (${report.accountIssues.size}):",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 240.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(report.accountIssues) { issue ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E20)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(issue.account.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                        if (issue.isMismatch) {
                                            Text("⚠️ Selisih", color = Color(0xFFFFB74D), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        } else {
                                            Text("✓ Konsisten", color = Color(0xFF81C784), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Saldo Tercatat:", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                                        Text(CurrencyFormatter.formatRupiah(issue.recordedBalance), color = Color.White, fontSize = 11.sp)
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Saldo Hitungan Transaksi:", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                                        Text(CurrencyFormatter.formatRupiah(issue.calculatedBalance), color = Color.White, fontSize = 11.sp)
                                    }

                                    if (issue.isMismatch) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Selisih Deviasi:", color = Color(0xFFFF8A80), fontSize = 11.sp)
                                            Text(CurrencyFormatter.formatRupiah(issue.difference), color = Color(0xFFFF8A80), fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onRefreshAudit,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF282828), contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Periksa Ulang", fontSize = 12.sp)
                }

                if (report?.isHealthy == false) {
                    Button(
                        onClick = onReconcileAll,
                        modifier = Modifier.testTag("reconcile_all_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sinkronkan Saldo", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup", color = Color.White.copy(alpha = 0.6f))
            }
        },
        containerColor = Color(0xFF141414),
        shape = RoundedCornerShape(20.dp)
    )
}
