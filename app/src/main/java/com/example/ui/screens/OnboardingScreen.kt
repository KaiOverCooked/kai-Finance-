package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.entity.AccountType
import com.example.data.local.entity.TransactionCategory
import com.example.ui.util.CurrencyFormatter
import com.example.ui.util.CurrencyFormatter.formatRupiah

@Composable
fun OnboardingScreen(
    onComplete: (
        accountName: String,
        accountType: AccountType,
        initialBalance: Double,
        budgetCategory: TransactionCategory?,
        budgetLimit: Double?
    ) -> Unit
) {
    var step by remember { mutableIntStateOf(1) }

    // Account creation state
    var accountType by remember { mutableStateOf(AccountType.BANK) }
    var accountName by remember { mutableStateOf("BCA Utama") }
    var startingBalanceInput by remember { mutableStateOf("2500000") }

    // Budget state
    var selectedBudgetCategory by remember { mutableStateOf<TransactionCategory?>(TransactionCategory.FOOD) }
    var budgetLimitInput by remember { mutableStateOf("2000000") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
            .padding(24.dp)
            .testTag("onboarding_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header / Progress Indicator
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(4) { index ->
                        val active = (index + 1) <= step
                        Box(
                            modifier = Modifier
                                .height(4.dp)
                                .width(if (index + 1 == step) 28.dp else 12.dp)
                                .clip(CircleShape)
                                .background(if (active) Color.White else Color.White.copy(alpha = 0.2f))
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Step Content
            AnimatedContent(
                targetState = step,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "onboarding_step_content"
            ) { currentStep ->
                when (currentStep) {
                    1 -> StepWelcome(onNext = { step = 2 })
                    2 -> StepAccount(
                        accountType = accountType,
                        onTypeChange = { accountType = it },
                        accountName = accountName,
                        onNameChange = { accountName = it },
                        balance = startingBalanceInput,
                        onBalanceChange = { startingBalanceInput = it },
                        onNext = { step = 3 }
                    )
                    3 -> StepBudget(
                        selectedCategory = selectedBudgetCategory,
                        onCategoryChange = { selectedBudgetCategory = it },
                        budgetLimit = budgetLimitInput,
                        onBudgetLimitChange = { budgetLimitInput = it },
                        onSkip = {
                            selectedBudgetCategory = null
                            budgetLimitInput = ""
                            step = 4
                        },
                        onNext = { step = 4 }
                    )
                    4 -> StepDone(
                        accountName = accountName,
                        accountType = accountType,
                        startingBalance = startingBalanceInput.toDoubleOrNull() ?: 0.0,
                        budgetCategory = selectedBudgetCategory,
                        budgetLimit = budgetLimitInput.toDoubleOrNull(),
                        onFinish = {
                            val bal = startingBalanceInput.toDoubleOrNull() ?: 0.0
                            val bLimit = budgetLimitInput.toDoubleOrNull()
                            onComplete(accountName, accountType, bal, selectedBudgetCategory, bLimit)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StepWelcome(onNext: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(RoundedCornerShape(28.dp))
                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(28.dp))
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_onboarding_hero),
                contentDescription = "Kai Finance Onboarding",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Welcome to Kai Finance",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Kelola rekening, hutang & piutang, transaksi rutin otomatis, investasi, dan budgeting tanpa tracking server luar.",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            lineHeight = 22.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(36.dp))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("onboarding_start_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Mulai Siapkan Akun", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun StepAccount(
    accountType: AccountType,
    onTypeChange: (AccountType) -> Unit,
    accountName: String,
    onNameChange: (String) -> Unit,
    balance: String,
    onBalanceChange: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Langkah 1 dari 2",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.5f),
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Buat Akun Pertamamu",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Pilih jenis akun dan saldo awalmu untuk pencatatan transaksi yang rapi.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.6f),
            modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
        )

        // Type Selector Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                AccountType.BANK to "Bank",
                AccountType.CASH to "Tunai",
                AccountType.EWALLET to "E-Wallet"
            ).forEach { (type, label) ->
                val selected = accountType == type
                FilterChip(
                    selected = selected,
                    onClick = {
                        onTypeChange(type)
                        if (accountName.isBlank() || accountName == "BCA Utama" || accountName == "Dompet Tunai" || accountName == "GoPay") {
                            onNameChange(
                                when (type) {
                                    AccountType.BANK -> "BCA Utama"
                                    AccountType.CASH -> "Dompet Tunai"
                                    AccountType.EWALLET -> "GoPay"
                                }
                            )
                        }
                    },
                    label = { Text(label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color.White,
                        selectedLabelColor = Color.Black,
                        containerColor = Color(0xFF1C1C1E),
                        labelColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Account Name Field
        Text("Nama Akun", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
        OutlinedTextField(
            value = accountName,
            onValueChange = onNameChange,
            placeholder = { Text("Contoh: BCA, Dompet Tunai, Mandiri") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("onboarding_account_name"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(14.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Starting Balance Field
        Text("Saldo Awal (Rp)", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
        OutlinedTextField(
            value = balance,
            onValueChange = { onBalanceChange(it.filter { char -> char.isDigit() || char == '.' }) },
            placeholder = { Text("0") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("onboarding_account_balance"),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(14.dp),
            singleLine = true
        )

        val parsedVal = balance.toDoubleOrNull() ?: 0.0
        Text(
            text = "Pratinjau: ${CurrencyFormatter.formatRupiah(parsedVal)}",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.5f),
            modifier = Modifier.padding(top = 4.dp, start = 4.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onNext,
            enabled = accountName.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("onboarding_to_budget_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Lanjut ke Anggaran", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(6.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun StepBudget(
    selectedCategory: TransactionCategory?,
    onCategoryChange: (TransactionCategory?) -> Unit,
    budgetLimit: String,
    onBudgetLimitChange: (String) -> Unit,
    onSkip: () -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Langkah 2 dari 2",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.5f),
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Atur Anggaran Bulanan",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Pantau batas pengeluaranmu. Kamu bisa melewati langkah ini jika belum ingin menetapkannya.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.6f),
            modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
        )

        Text("Pilih Kategori Utama", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                TransactionCategory.FOOD to "Makanan",
                TransactionCategory.TRANSPORT to "Transport",
                TransactionCategory.UTILITIES to "Tagihan",
                TransactionCategory.ENTERTAINMENT to "Hiburan"
            ).forEach { (cat, label) ->
                val sel = selectedCategory == cat
                FilterChip(
                    selected = sel,
                    onClick = { onCategoryChange(cat) },
                    label = { Text(label, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color.White,
                        selectedLabelColor = Color.Black,
                        containerColor = Color(0xFF1C1C1E),
                        labelColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text("Batas Anggaran Bulanan (Rp)", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
        OutlinedTextField(
            value = budgetLimit,
            onValueChange = { onBudgetLimitChange(it.filter { char -> char.isDigit() || char == '.' }) },
            placeholder = { Text("Contoh: 2000000") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("onboarding_budget_limit"),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(14.dp),
            singleLine = true
        )

        val parsedVal = budgetLimit.toDoubleOrNull() ?: 0.0
        Text(
            text = "Batas: ${CurrencyFormatter.formatRupiah(parsedVal)} per bulan",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.5f),
            modifier = Modifier.padding(top = 4.dp, start = 4.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("onboarding_save_budget_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Simpan & Lanjut", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        TextButton(
            onClick = onSkip,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Lewati Langkah Ini", color = Color.White.copy(alpha = 0.6f))
        }
    }
}

@Composable
private fun StepDone(
    accountName: String,
    accountType: AccountType,
    startingBalance: Double,
    budgetCategory: TransactionCategory?,
    budgetLimit: Double?,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.Black, modifier = Modifier.size(44.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Semua Siap!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = "Data awalmu telah terkonfigurasi. Selamat mengelola keuangan!",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        // Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF18181A)),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            when (accountType) {
                                AccountType.BANK -> Icons.Default.AccountBalance
                                AccountType.CASH -> Icons.Default.AccountBalanceWallet
                                AccountType.EWALLET -> Icons.Default.Savings
                            },
                            contentDescription = null,
                            tint = Color.White
                        )
                        Column {
                            Text(accountName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                            Text(accountType.name, color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                        }
                    }
                    Text(
                        CurrencyFormatter.formatRupiah(startingBalance),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 15.sp
                    )
                }

                if (budgetCategory != null && budgetLimit != null && budgetLimit > 0) {
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.1f)))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PieChart, contentDescription = null, tint = Color.White)
                            Column {
                                Text("Budget ${budgetCategory.name}", fontWeight = FontWeight.SemiBold, color = Color.White, fontSize = 13.sp)
                                Text("Bulanan", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
                            }
                        }
                        Text(
                            CurrencyFormatter.formatRupiah(budgetLimit),
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        Button(
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("onboarding_finish_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Masuk ke Dashboard", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}
