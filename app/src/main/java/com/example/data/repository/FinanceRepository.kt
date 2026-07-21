package com.example.data.repository

import com.example.data.local.KaiDatabase
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.GoalEntity
import com.example.data.local.entity.NotificationEntity
import com.example.data.local.entity.NotificationType
import com.example.data.local.entity.TransactionCategory
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Calendar

class FinanceRepository(private val db: KaiDatabase) {

    val allTransactions: Flow<List<TransactionEntity>> = db.transactionDao().getAllTransactions()
    val allBudgets: Flow<List<BudgetEntity>> = db.budgetDao().getAllBudgets()
    val allGoals: Flow<List<GoalEntity>> = db.goalDao().getAllGoals()
    val allNotifications: Flow<List<NotificationEntity>> = db.notificationDao().getAllNotifications()

    suspend fun addTransaction(transaction: TransactionEntity): Long {
        val id = db.transactionDao().insertTransaction(transaction)
        checkBudgetAlerts(transaction)
        return id
    }

    suspend fun updateTransaction(transaction: TransactionEntity) {
        db.transactionDao().updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        db.transactionDao().deleteTransaction(transaction)
    }

    suspend fun deleteTransactionById(id: Long) {
        db.transactionDao().deleteTransactionById(id)
    }

    suspend fun addBudget(budget: BudgetEntity): Long {
        return db.budgetDao().insertBudget(budget)
    }

    suspend fun updateBudget(budget: BudgetEntity) {
        db.budgetDao().updateBudget(budget)
    }

    suspend fun deleteBudgetById(id: Long) {
        db.budgetDao().deleteBudgetById(id)
    }

    suspend fun addGoal(goal: GoalEntity): Long {
        return db.goalDao().insertGoal(goal)
    }

    suspend fun updateGoal(goal: GoalEntity) {
        db.goalDao().updateGoal(goal)
    }

    suspend fun deleteGoalById(id: Long) {
        db.goalDao().deleteGoalById(id)
    }

    suspend fun markNotificationAsRead(id: Long) {
        db.notificationDao().markAsRead(id)
    }

    suspend fun markAllNotificationsAsRead() {
        db.notificationDao().markAllAsRead()
    }

    private suspend fun checkBudgetAlerts(transaction: TransactionEntity) {
        if (transaction.type == TransactionType.EXPENSE) {
            val budgets = db.budgetDao().getAllBudgets().first()
            val categoryBudget = budgets.find { it.category == transaction.category }
            if (categoryBudget != null) {
                val transactions = db.transactionDao().getAllTransactions().first()
                val totalCategorySpent = transactions
                    .filter { it.type == TransactionType.EXPENSE && it.category == transaction.category }
                    .sumOf { it.amount }

                val updatedBudget = categoryBudget.copy(spentAmount = totalCategorySpent)
                db.budgetDao().updateBudget(updatedBudget)

                val ratio = if (categoryBudget.limitAmount > 0) totalCategorySpent / categoryBudget.limitAmount else 0.0
                if (ratio >= 1.0) {
                    db.notificationDao().insertNotification(
                        NotificationEntity(
                            title = "Budget Exceeded!",
                            message = "You have exceeded your monthly budget for ${transaction.category.name.lowercase().replaceFirstChar { it.uppercase() }}.",
                            type = NotificationType.BUDGET_ALERT
                        )
                    )
                } else if (ratio >= 0.8) {
                    db.notificationDao().insertNotification(
                        NotificationEntity(
                            title = "Budget Warning",
                            message = "You are at ${(ratio * 100).toInt()}% of your budget for ${transaction.category.name.lowercase().replaceFirstChar { it.uppercase() }}.",
                            type = NotificationType.BUDGET_ALERT
                        )
                    )
                }
            }
        }
    }

    suspend fun seedInitialDataIfEmpty() {
        val existingTransactions = db.transactionDao().getAllTransactions().first()
        if (existingTransactions.isNotEmpty()) return

        val now = System.currentTimeMillis()
        val dayMs = 24 * 60 * 60 * 1000L

        // Initial Transactions
        val sampleTransactions = listOf(
            TransactionEntity(title = "Monthly Salary", amount = 8500.00, type = TransactionType.INCOME, category = TransactionCategory.SALARY, timestamp = now - 1 * dayMs, note = "Main tech company salary"),
            TransactionEntity(title = "Apple Store Hardware", amount = 1299.00, type = TransactionType.EXPENSE, category = TransactionCategory.SHOPPING, timestamp = now - 2 * dayMs, note = "MacBook Air M3 purchase"),
            TransactionEntity(title = "Whole Foods Organic Market", amount = 184.50, type = TransactionType.EXPENSE, category = TransactionCategory.FOOD, timestamp = now - 3 * dayMs, note = "Weekly groceries"),
            TransactionEntity(title = "Luxury Apartment Rent", amount = 2800.00, type = TransactionType.EXPENSE, category = TransactionCategory.HOUSING, timestamp = now - 5 * dayMs, note = "Downtown studio rent"),
            TransactionEntity(title = "Stock Investment Dividend", amount = 420.00, type = TransactionType.INCOME, category = TransactionCategory.INVESTMENT, timestamp = now - 6 * dayMs, note = "S&P 500 quarterly payout"),
            TransactionEntity(title = "Electric & Utilities", amount = 145.20, type = TransactionType.EXPENSE, category = TransactionCategory.UTILITIES, timestamp = now - 8 * dayMs, note = "Power & high-speed fiber"),
            TransactionEntity(title = "Equinox Gym Membership", amount = 260.00, type = TransactionType.EXPENSE, category = TransactionCategory.HEALTH, timestamp = now - 10 * dayMs, note = "Monthly fitness subscription"),
            TransactionEntity(title = "Linear & Figma Pro", amount = 45.00, type = TransactionType.EXPENSE, category = TransactionCategory.ENTERTAINMENT, timestamp = now - 12 * dayMs, note = "Software suite subscriptions")
        )

        sampleTransactions.forEach { db.transactionDao().insertTransaction(it) }

        // Initial Budgets
        val sampleBudgets = listOf(
            BudgetEntity(category = TransactionCategory.HOUSING, limitAmount = 3000.0, spentAmount = 2800.0),
            BudgetEntity(category = TransactionCategory.FOOD, limitAmount = 800.0, spentAmount = 184.50),
            BudgetEntity(category = TransactionCategory.SHOPPING, limitAmount = 1500.0, spentAmount = 1299.00),
            BudgetEntity(category = TransactionCategory.ENTERTAINMENT, limitAmount = 300.0, spentAmount = 45.00),
            BudgetEntity(category = TransactionCategory.UTILITIES, limitAmount = 250.0, spentAmount = 145.20)
        )
        sampleBudgets.forEach { db.budgetDao().insertBudget(it) }

        // Initial Savings Goals
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, 6)
        val sampleGoals = listOf(
            GoalEntity(name = "Emergency Fund", targetAmount = 20000.0, currentAmount = 14500.0, targetDateMillis = cal.timeInMillis, category = "Safety"),
            GoalEntity(name = "Japan Spring Trip", targetAmount = 6000.0, currentAmount = 4200.0, targetDateMillis = cal.timeInMillis, category = "Travel"),
            GoalEntity(name = "Angel Investment Fund", targetAmount = 15000.0, currentAmount = 5000.0, targetDateMillis = cal.timeInMillis, category = "Investing")
        )
        sampleGoals.forEach { db.goalDao().insertGoal(it) }

        // Initial Notifications
        val sampleNotifications = listOf(
            NotificationEntity(title = "Welcome to Kai Finance", message = "Experience ultra-clean monochrome personal finance with AI reasoning.", timestamp = now, type = NotificationType.SYSTEM),
            NotificationEntity(title = "Upcoming Rent Bill", message = "Your next rent payment of $2,800 is scheduled for the 1st.", timestamp = now - dayMs, type = NotificationType.BILL_REMINDER),
            NotificationEntity(title = "Goal Progress!", message = "You've reached 70% of your Japan Spring Trip saving goal.", timestamp = now - 2 * dayMs, type = NotificationType.SAVINGS_MILESTONE)
        )
        sampleNotifications.forEach { db.notificationDao().insertNotification(it) }
    }
}
