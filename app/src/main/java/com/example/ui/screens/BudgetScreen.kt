package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.TransactionCategory
import com.example.ui.components.GlassCard
import com.example.ui.components.GoalProgressBar
import java.util.Locale

@Composable
fun BudgetScreen(
    budgets: List<BudgetEntity>,
    currencySymbol: String,
    onCreateBudget: (TransactionCategory, Double) -> Unit,
    onDeleteBudget: (Long) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .testTag("budget_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Text(
                text = "ALLOCATION",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Monthly Budgets",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Total Budget Summary
            val totalLimit = budgets.sumOf { it.limitAmount }
            val totalSpent = budgets.sumOf { it.spentAmount }
            val remaining = (totalLimit - totalSpent).coerceAtLeast(0.0)

            GlassCard(
                modifier = Modifier.padding(bottom = 16.dp),
                testTag = "budget_summary_card"
            ) {
                Column {
                    Text(
                        text = "TOTAL BUDGET REMAINING",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "$currencySymbol${String.format(Locale.US, "%,.2f", remaining)}",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Spent: $currencySymbol${String.format(Locale.US, "%,.0f", totalSpent)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Limit: $currencySymbol${String.format(Locale.US, "%,.0f", totalLimit)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    val overallProgress = if (totalLimit > 0) (totalSpent / totalLimit).toFloat() else 0f
                    GoalProgressBar(progress = overallProgress)
                }
            }

            Text(
                text = "Category Limits",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Budget Items List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("budgets_list")
            ) {
                if (budgets.isEmpty()) {
                    item {
                        GlassCard {
                            Text(
                                text = "No active category budgets set. Tap '+' to create one.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(budgets, key = { it.id }) { budget ->
                        BudgetItemRow(
                            budget = budget,
                            currencySymbol = currencySymbol,
                            onDelete = { onDeleteBudget(budget.id) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

        // Add FAB
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 70.dp, end = 8.dp)
                .testTag("fab_add_budget"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = CircleShape
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Budget")
        }

        if (showAddDialog) {
            AddBudgetSheet(
                onDismiss = { showAddDialog = false },
                onSave = { category, limit ->
                    onCreateBudget(category, limit)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun BudgetItemRow(
    budget: BudgetEntity,
    currencySymbol: String,
    onDelete: () -> Unit
) {
    val progress = if (budget.limitAmount > 0) (budget.spentAmount / budget.limitAmount).toFloat() else 0f
    val isAlert = progress >= 0.8f
    val isExceeded = progress >= 1.0f

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        testTag = "budget_item_${budget.id}"
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isAlert) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Alert",
                            tint = if (isExceeded) Color.Red else Color(0xFFFB8C00),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                    Text(
                        text = budget.category.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$currencySymbol${String.format(Locale.US, "%,.0f", budget.spentAmount)} / $currencySymbol${String.format(Locale.US, "%,.0f", budget.limitAmount)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isExceeded) Color.Red else MaterialTheme.colorScheme.onSurface
                    )

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.testTag("delete_budget_${budget.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            GoalProgressBar(
                progress = progress,
                progressColor = if (isExceeded) Color.Red else if (isAlert) Color(0xFFFB8C00) else MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBudgetSheet(
    onDismiss: () -> Unit,
    onSave: (TransactionCategory, Double) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var category by remember { mutableStateOf(TransactionCategory.FOOD) }
    var limitStr by remember { mutableStateOf("") }
    var showCategoryDropdown by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .testTag("add_budget_sheet")
        ) {
            Text(
                text = "Set Category Budget",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box {
                OutlinedTextField(
                    value = category.name.lowercase().replaceFirstChar { it.uppercase() },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCategoryDropdown = true }
                        .testTag("input_budget_category"),
                    trailingIcon = {
                        Icon(imageVector = Icons.Default.FilterList, contentDescription = null)
                    }
                )

                DropdownMenu(
                    expanded = showCategoryDropdown,
                    onDismissRequest = { showCategoryDropdown = false }
                ) {
                    TransactionCategory.entries.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            onClick = {
                                category = cat
                                showCategoryDropdown = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = limitStr,
                onValueChange = { limitStr = it },
                label = { Text("Monthly Limit ($)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_budget_limit"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    val limit = limitStr.toDoubleOrNull() ?: 0.0
                    if (limit > 0.0) {
                        onSave(category, limit)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_budget_btn"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Save Budget", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
