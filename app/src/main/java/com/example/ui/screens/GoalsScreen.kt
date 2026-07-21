package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
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
import com.example.data.local.entity.GoalEntity
import com.example.ui.components.GlassCard
import com.example.ui.components.GoalProgressBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GoalsScreen(
    goals: List<GoalEntity>,
    currencySymbol: String,
    onCreateGoal: (String, Double, Double, Long, String) -> Unit,
    onContribute: (GoalEntity, Double) -> Unit,
    onDeleteGoal: (Long) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var contributingGoal by remember { mutableStateOf<GoalEntity?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .testTag("goals_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "MILESTONES",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Savings Goals",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Overall Goals Progress Header Card
            val totalTarget = goals.sumOf { it.targetAmount }
            val totalSaved = goals.sumOf { it.currentAmount }
            val overallRatio = if (totalTarget > 0) (totalSaved / totalTarget).toFloat() else 0f

            GlassCard(
                modifier = Modifier.padding(bottom = 16.dp),
                testTag = "goals_summary_card"
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TOTAL CAPITAL SAVED",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${goals.count { it.isAchieved }} Achieved",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "$currencySymbol${String.format(Locale.US, "%,.2f", totalSaved)}",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Target Pool: $currencySymbol${String.format(Locale.US, "%,.0f", totalTarget)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    GoalProgressBar(progress = overallRatio)
                }
            }

            Text(
                text = "Active Goals",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("goals_list")
            ) {
                if (goals.isEmpty()) {
                    item {
                        GlassCard {
                            Text(
                                text = "No savings goals created yet. Tap '+' to create your first goal.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(goals, key = { it.id }) { goal ->
                        GoalItemRow(
                            goal = goal,
                            currencySymbol = currencySymbol,
                            onContributeClick = { contributingGoal = goal },
                            onDelete = { onDeleteGoal(goal.id) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 70.dp, end = 8.dp)
                .testTag("fab_add_goal"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = CircleShape
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "New Goal")
        }

        if (showAddDialog) {
            AddGoalSheet(
                onDismiss = { showAddDialog = false },
                onSave = { name, target, current, category ->
                    val now = System.currentTimeMillis()
                    val targetDate = now + 90 * 24 * 60 * 60 * 1000L
                    onCreateGoal(name, target, current, targetDate, category)
                    showAddDialog = false
                }
            )
        }

        contributingGoal?.let { goal ->
            ContributeGoalSheet(
                goal = goal,
                currencySymbol = currencySymbol,
                onDismiss = { contributingGoal = null },
                onDeposit = { amount ->
                    onContribute(goal, amount)
                    contributingGoal = null
                }
            )
        }
    }
}

@Composable
fun GoalItemRow(
    goal: GoalEntity,
    currencySymbol: String,
    onContributeClick: () -> Unit,
    onDelete: () -> Unit
) {
    val progress = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount).toFloat() else 0f
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.US)
    val dateStr = sdf.format(Date(goal.targetDateMillis))

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        testTag = "goal_item_${goal.id}"
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = if (goal.isAchieved) Color(0xFFFFD700).copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (goal.isAchieved) Icons.Default.CheckCircle else Icons.Default.Flag,
                                contentDescription = null,
                                tint = if (goal.isAchieved) Color(0xFFFFD700) else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = goal.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Target: $dateStr • ${goal.category}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_goal_${goal.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$currencySymbol${String.format(Locale.US, "%,.0f", goal.currentAmount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$currencySymbol${String.format(Locale.US, "%,.0f", goal.targetAmount)} (${(progress * 100).toInt()}%)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            GoalProgressBar(progress = progress)

            Spacer(modifier = Modifier.height(12.dp))

            if (!goal.isAchieved) {
                OutlinedButton(
                    onClick = onContributeClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("contribute_goal_${goal.id}"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("+ Deposit Funds")
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFFD700).copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "🏆 Achievement Unlocked!",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalSheet(
    onDismiss: () -> Unit,
    onSave: (String, Double, Double, String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var name by remember { mutableStateOf("") }
    var targetStr by remember { mutableStateOf("") }
    var currentStr by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Investment") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .testTag("add_goal_sheet")
        ) {
            Text(
                text = "New Savings Goal",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Goal Name") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_goal_name"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = targetStr,
                onValueChange = { targetStr = it },
                label = { Text("Target Amount ($)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_goal_target"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = currentStr,
                onValueChange = { currentStr = it },
                label = { Text("Initial Deposit ($)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_goal_current"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Category / Purpose") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_goal_category"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    val target = targetStr.toDoubleOrNull() ?: 0.0
                    val current = currentStr.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && target > 0.0) {
                        onSave(name, target, current, category)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_goal_btn"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Create Goal", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContributeGoalSheet(
    goal: GoalEntity,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onDeposit: (Double) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    var depositStr by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .testTag("contribute_goal_sheet")
        ) {
            Text(
                text = "Deposit to ${goal.name}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = depositStr,
                onValueChange = { depositStr = it },
                label = { Text("Deposit Amount ($currencySymbol)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_deposit_amount"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    val amt = depositStr.toDoubleOrNull() ?: 0.0
                    if (amt > 0.0) {
                        onDeposit(amt)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_deposit_btn"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Confirm Deposit", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
