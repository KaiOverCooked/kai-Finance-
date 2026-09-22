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
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import com.example.data.local.entity.InvestmentEntity
import com.example.data.local.entity.InvestmentType
import com.example.ui.components.GlassCard
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import java.util.Locale

@Composable
fun InvestmentsScreen(
    investments: List<InvestmentEntity>,
    currencySymbol: String,
    onCreateInvestment: (String, String, Double, Double, Double, InvestmentType, String) -> Unit,
    onUpdatePrice: (Long, Double) -> Unit,
    onAddDividend: (Long, Double) -> Unit,
    onDeleteInvestment: (Long) -> Unit,
    onBack: (() -> Unit)? = null
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingAsset by remember { mutableStateOf<InvestmentEntity?>(null) }
    var dividendAsset by remember { mutableStateOf<InvestmentEntity?>(null) }

    val totalPortfolioValue = investments.sumOf { it.currentValue }
    val totalCost = investments.sumOf { it.totalCost }
    val totalGainLoss = totalPortfolioValue - totalCost
    val gainLossPct = if (totalCost > 0) (totalGainLoss / totalCost) * 100 else 0.0
    val totalDividends = investments.sumOf { it.dividendReceived }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("investments_screen")
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
                        text = "INVESTMENT PORTFOLIO",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Saham, Kripto, Reksadana, SBN & Dividen",
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
                    modifier = Modifier.testTag("add_investment_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Investment", tint = Color.Black)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Portfolio Hero Card
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            testTag = "portfolio_hero_card"
        ) {
            Column {
                Text("NILAI PORTOFOLIO", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f), letterSpacing = 2.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "$currencySymbol${String.format(Locale.US, "%,.2f", totalPortfolioValue)}",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Light,
                    letterSpacing = (-1).sp
                )
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Total Return (P/L)", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (totalGainLoss >= 0) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                                contentDescription = null,
                                tint = if (totalGainLoss >= 0) IncomeGreen else ExpenseRed,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${if (totalGainLoss >= 0) "+" else ""}$currencySymbol${String.format(Locale.US, "%,.2f", totalGainLoss)} (${String.format(Locale.US, "%.2f", gainLossPct)}%)",
                                color = if (totalGainLoss >= 0) IncomeGreen else ExpenseRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text("Total Dividen", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f), fontSize = 10.sp)
                        Text(
                            text = "+$currencySymbol${String.format(Locale.US, "%,.2f", totalDividends)}",
                            color = IncomeGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Asset List
        if (investments.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Belum ada aset investasi terdaftar", color = Color.White.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                    ) {
                        Text("Tambah Aset Pertama")
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(investments, key = { it.id }) { asset ->
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color.Black)
                                            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (asset.type == InvestmentType.CRYPTO) Icons.Default.CurrencyBitcoin else Icons.Default.ShowChart,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(asset.symbol, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                                        Text(
                                            "${asset.assetName} • ${asset.quantity} unit",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.45f)
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "$currencySymbol${String.format(Locale.US, "%,.2f", asset.currentValue)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${if (asset.unrealizedGainLoss >= 0) "+" else ""}${String.format(Locale.US, "%.2f", asset.gainLossPercentage)}%",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (asset.unrealizedGainLoss >= 0) IncomeGreen else ExpenseRed,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Details Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Beli: $currencySymbol${String.format(Locale.US, "%,.2f", asset.buyPrice)}",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = "Sekarang: $currencySymbol${String.format(Locale.US, "%,.2f", asset.currentPrice)}",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.6f)
                                )
                                if (asset.dividendReceived > 0) {
                                    Text(
                                        text = "Dividen: +$currencySymbol${String.format(Locale.US, "%,.2f", asset.dividendReceived)}",
                                        fontSize = 12.sp,
                                        color = IncomeGreen
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { editingAsset = asset },
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F1F1F), contentColor = Color.White),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Ubah Harga", fontSize = 11.sp)
                                }

                                Button(
                                    onClick = { dividendAsset = asset },
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F1F1F), contentColor = Color.White),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.AttachMoney, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("+ Dividen", fontSize = 11.sp)
                                }

                                IconButton(
                                    onClick = { onDeleteInvestment(asset.id) },
                                    modifier = Modifier.size(36.dp)
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

    // Add Investment Dialog
    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var symbol by remember { mutableStateOf("") }
        var qtyStr by remember { mutableStateOf("") }
        var buyPriceStr by remember { mutableStateOf("") }
        var currPriceStr by remember { mutableStateOf("") }
        var assetType by remember { mutableStateOf(InvestmentType.STOCK) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Tambah Aset Investasi", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(InvestmentType.STOCK, InvestmentType.CRYPTO, InvestmentType.MUTUAL_FUND, InvestmentType.BOND).forEach { t ->
                            Button(
                                onClick = { assetType = t },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (assetType == t) Color.White else Color(0xFF1F1F1F),
                                    contentColor = if (assetType == t) Color.Black else Color.White
                                ),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 2.dp, vertical = 4.dp)
                            ) {
                                Text(t.name.take(5), fontSize = 10.sp)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = symbol,
                        onValueChange = { symbol = it },
                        label = { Text("Kode Simbol (e.g. BBCA, VOO, BTC)") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nama Lengkap Aset") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    OutlinedTextField(
                        value = qtyStr,
                        onValueChange = { qtyStr = it },
                        label = { Text("Jumlah Unit / Lembar") },
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
                        value = buyPriceStr,
                        onValueChange = { buyPriceStr = it },
                        label = { Text("Harga Beli Rata-Rata") },
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
                        value = currPriceStr,
                        onValueChange = { currPriceStr = it },
                        label = { Text("Harga Sekarang") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
                        val qty = qtyStr.toDoubleOrNull() ?: 0.0
                        val buy = buyPriceStr.toDoubleOrNull() ?: 0.0
                        val curr = currPriceStr.toDoubleOrNull() ?: buy
                        if (symbol.isNotBlank() && qty > 0) {
                            onCreateInvestment(name.ifBlank { symbol }, symbol, qty, buy, curr, assetType, "")
                            showAddDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                ) {
                    Text("Simpan Aset")
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

    // Edit Price Dialog
    if (editingAsset != null) {
        val asset = editingAsset!!
        var newPriceStr by remember { mutableStateOf(asset.currentPrice.toString()) }

        AlertDialog(
            onDismissRequest = { editingAsset = null },
            title = { Text("Update Harga: ${asset.symbol}", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = newPriceStr,
                    onValueChange = { newPriceStr = it },
                    label = { Text("Harga Pasar Terkini") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val np = newPriceStr.toDoubleOrNull() ?: asset.currentPrice
                        onUpdatePrice(asset.id, np)
                        editingAsset = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                ) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingAsset = null }) {
                    Text("Batal", color = Color.White.copy(alpha = 0.6f))
                }
            },
            containerColor = Color(0xFF141414)
        )
    }

    // Dividend Dialog
    if (dividendAsset != null) {
        val asset = dividendAsset!!
        var divAmountStr by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { dividendAsset = null },
            title = { Text("Catat Dividen: ${asset.symbol}", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = divAmountStr,
                    onValueChange = { divAmountStr = it },
                    label = { Text("Nominal Dividen Diterima") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = divAmountStr.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            onAddDividend(asset.id, amt)
                            dividendAsset = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                ) {
                    Text("Simpan Dividen")
                }
            },
            dismissButton = {
                TextButton(onClick = { dividendAsset = null }) {
                    Text("Batal", color = Color.White.copy(alpha = 0.6f))
                }
            },
            containerColor = Color(0xFF141414)
        )
    }
}
