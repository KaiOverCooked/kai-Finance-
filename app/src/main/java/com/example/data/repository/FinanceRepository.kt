package com.example.data.repository

import com.example.data.local.KaiDatabase
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.AccountType
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.DebtEntity
import com.example.data.local.entity.DebtPaymentEntity
import com.example.data.local.entity.DebtType
import com.example.data.local.entity.GoalEntity
import com.example.data.local.entity.InvestmentEntity
import com.example.data.local.entity.InvestmentType
import com.example.data.local.entity.NotificationEntity
import com.example.data.local.entity.NotificationType
import com.example.data.local.entity.RecurringCategoryType
import com.example.data.local.entity.RecurringEntity
import com.example.data.local.entity.RecurringFrequency
import com.example.data.local.entity.TransactionCategory
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

class FinanceRepository(private val db: KaiDatabase) {

    // 1. Core Flows
    val allTransactions: Flow<List<TransactionEntity>> = db.transactionDao().getAllTransactions()
    val allBudgets: Flow<List<BudgetEntity>> = db.budgetDao().getAllBudgets()
    val allGoals: Flow<List<GoalEntity>> = db.goalDao().getAllGoals()
    val allNotifications: Flow<List<NotificationEntity>> = db.notificationDao().getAllNotifications()
    val allAccounts: Flow<List<AccountEntity>> = db.accountDao().getAllAccounts()
    val allDebts: Flow<List<DebtEntity>> = db.debtDao().getAllDebts()
    val allRecurring: Flow<List<RecurringEntity>> = db.recurringDao().getAllRecurring()
    val allInvestments: Flow<List<InvestmentEntity>> = db.investmentDao().getAllInvestments()

    fun getPaymentsForDebt(debtId: Long): Flow<List<DebtPaymentEntity>> = db.debtDao().getPaymentsForDebt(debtId)

    // 2. Transactions & Account Balance Synchronization
    suspend fun addTransaction(transaction: TransactionEntity): Long {
        val id = db.transactionDao().insertTransaction(transaction)
        // If tied to an account and not a transfer (transfers manage balances separately)
        if (transaction.accountId != null && !transaction.isTransfer) {
            val delta = if (transaction.type == TransactionType.INCOME) transaction.amount else -transaction.amount
            db.accountDao().updateBalance(transaction.accountId, delta)
        }
        checkBudgetAlerts(transaction)
        return id
    }

    suspend fun updateTransaction(newTx: TransactionEntity) {
        val oldTx = db.transactionDao().getTransactionById(newTx.id)
        if (oldTx != null) {
            // Revert old transaction from old account if not transfer
            if (oldTx.accountId != null && !oldTx.isTransfer) {
                val revertDelta = if (oldTx.type == TransactionType.INCOME) -oldTx.amount else oldTx.amount
                db.accountDao().updateBalance(oldTx.accountId, revertDelta)
            }
            // Apply new transaction to new account if not transfer
            if (newTx.accountId != null && !newTx.isTransfer) {
                val applyDelta = if (newTx.type == TransactionType.INCOME) newTx.amount else -newTx.amount
                db.accountDao().updateBalance(newTx.accountId, applyDelta)
            }
        }
        db.transactionDao().updateTransaction(newTx)
        checkBudgetAlerts(newTx)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        if (transaction.accountId != null && !transaction.isTransfer) {
            val reverseDelta = if (transaction.type == TransactionType.INCOME) -transaction.amount else transaction.amount
            db.accountDao().updateBalance(transaction.accountId, reverseDelta)
        }
        db.transactionDao().deleteTransaction(transaction)
    }

    suspend fun deleteTransactionById(id: Long) {
        val tx = db.transactionDao().getTransactionById(id)
        if (tx != null) {
            deleteTransaction(tx)
        }
    }

    // 3. Accounts & Transfers (Transfers are NOT real income or expenses)
    suspend fun addAccount(account: AccountEntity): Long = db.accountDao().insertAccount(account)
    suspend fun updateAccount(account: AccountEntity) = db.accountDao().updateAccount(account)
    suspend fun deleteAccountById(id: Long) = db.accountDao().deleteAccountById(id)
    suspend fun getAccountById(id: Long): AccountEntity? = db.accountDao().getAccountById(id)

    suspend fun transferBetweenAccounts(fromAccountId: Long, toAccountId: Long, amount: Double, note: String = ""): Boolean {
        val fromAccount = db.accountDao().getAccountById(fromAccountId) ?: return false
        val toAccount = db.accountDao().getAccountById(toAccountId) ?: return false

        // Deduct from source
        db.accountDao().updateBalance(fromAccountId, -amount)
        // Add to destination
        db.accountDao().updateBalance(toAccountId, amount)

        val now = System.currentTimeMillis()
        // Record outbound transfer (marked isTransfer = true)
        db.transactionDao().insertTransaction(
            TransactionEntity(
                title = "Transfer to ${toAccount.name}",
                amount = amount,
                type = TransactionType.EXPENSE,
                category = TransactionCategory.OTHER,
                timestamp = now,
                note = if (note.isNotBlank()) note else "Transfer: ${fromAccount.name} → ${toAccount.name}",
                accountId = fromAccountId,
                transferToAccountId = toAccountId,
                isTransfer = true
            )
        )

        // Record inbound transfer (marked isTransfer = true)
        db.transactionDao().insertTransaction(
            TransactionEntity(
                title = "Transfer from ${fromAccount.name}",
                amount = amount,
                type = TransactionType.INCOME,
                category = TransactionCategory.OTHER,
                timestamp = now + 1,
                note = if (note.isNotBlank()) note else "Received from ${fromAccount.name}",
                accountId = toAccountId,
                transferToAccountId = fromAccountId,
                isTransfer = true
            )
        )
        return true
    }

    // 4. Hutang & Piutang (Debts & Receivables)
    suspend fun addDebt(debt: DebtEntity): Long = db.debtDao().insertDebt(debt)
    suspend fun updateDebt(debt: DebtEntity) = db.debtDao().updateDebt(debt)
    suspend fun deleteDebtById(id: Long) = db.debtDao().deleteDebtById(id)

    suspend fun recordDebtPayment(debtId: Long, paymentAmount: Double, note: String = "", accountId: Long? = null) {
        val debt = db.debtDao().getDebtById(debtId) ?: return
        db.debtDao().recordPayment(debtId, paymentAmount)
        db.debtDao().insertPayment(
            DebtPaymentEntity(
                debtId = debtId,
                amount = paymentAmount,
                note = note.ifBlank { "Pembayaran untuk ${debt.personName}" }
            )
        )

        val txType = if (debt.type == DebtType.HUTANG) TransactionType.EXPENSE else TransactionType.INCOME
        val txTitle = if (debt.type == DebtType.HUTANG) "Bayar Hutang ke ${debt.personName}" else "Terima Piutang dari ${debt.personName}"
        addTransaction(
            TransactionEntity(
                title = txTitle,
                amount = paymentAmount,
                type = txType,
                category = TransactionCategory.OTHER,
                note = note.ifBlank { "Cicilan/pelunasan ${debt.personName}" },
                accountId = accountId
            )
        )
    }

    // 5. Recurring Automation & Account Integration
    suspend fun addRecurring(recurring: RecurringEntity): Long = db.recurringDao().insertRecurring(recurring)
    suspend fun updateRecurring(recurring: RecurringEntity) = db.recurringDao().updateRecurring(recurring)
    suspend fun deleteRecurringById(id: Long) = db.recurringDao().deleteRecurringById(id)

    fun calculateNextDueDate(fromMillis: Long, frequency: RecurringFrequency): Long {
        val cal = Calendar.getInstance().apply { timeInMillis = fromMillis }
        when (frequency) {
            RecurringFrequency.DAILY -> cal.add(Calendar.DAY_OF_YEAR, 1)
            RecurringFrequency.WEEKLY -> cal.add(Calendar.DAY_OF_YEAR, 7)
            RecurringFrequency.MONTHLY -> cal.add(Calendar.MONTH, 1)
            RecurringFrequency.YEARLY -> cal.add(Calendar.YEAR, 1)
        }
        return cal.timeInMillis
    }

    suspend fun executeRecurringItem(recurringId: Long): Boolean {
        val item = db.recurringDao().getRecurringById(recurringId) ?: return false
        val now = System.currentTimeMillis()

        // Create transaction connected to account
        addTransaction(
            TransactionEntity(
                title = item.title,
                amount = item.amount,
                type = item.type,
                category = item.category,
                timestamp = item.nextDueDateMillis.coerceAtMost(now),
                note = "Recurring [${item.recurringCategory.name}]: ${item.frequency.name.lowercase().replaceFirstChar { it.uppercase() }}",
                accountId = item.accountId,
                isRecurring = true
            )
        )

        // Advance next due date accurately based on frequency (at least one cycle into the future)
        var nextDue = calculateNextDueDate(item.nextDueDateMillis, item.frequency)
        while (nextDue <= now) {
            nextDue = calculateNextDueDate(nextDue, item.frequency)
        }

        db.recurringDao().markExecuted(recurringId, now, nextDue)
        return true
    }

    suspend fun processDueRecurringTransactions(): Int {
        val activeRecurring = db.recurringDao().getActiveRecurring().first()
        val now = System.currentTimeMillis()
        var count = 0

        for (item in activeRecurring) {
            if (item.autoExecute && item.nextDueDateMillis <= now) {
                executeRecurringItem(item.id)
                count++
            }
        }
        return count
    }

    // 6. Investments
    suspend fun addInvestment(investment: InvestmentEntity): Long = db.investmentDao().insertInvestment(investment)
    suspend fun updateInvestment(investment: InvestmentEntity) = db.investmentDao().updateInvestment(investment)
    suspend fun deleteInvestmentById(id: Long) = db.investmentDao().deleteInvestmentById(id)
    suspend fun updateInvestmentPrice(id: Long, newPrice: Double) = db.investmentDao().updateCurrentPrice(id, newPrice)
    suspend fun recordDividend(id: Long, dividendAmount: Double, accountId: Long? = null) {
        val inv = db.investmentDao().getInvestmentById(id) ?: return
        db.investmentDao().addDividend(id, dividendAmount)
        addTransaction(
            TransactionEntity(
                title = "Dividen: ${inv.symbol}",
                amount = dividendAmount,
                type = TransactionType.INCOME,
                category = TransactionCategory.INVESTMENT,
                note = "Dividen dari ${inv.assetName}",
                accountId = accountId
            )
        )
    }

    // 7. Budgets (Calculated strictly by period and excluding transfers)
    suspend fun addBudget(budget: BudgetEntity): Long = db.budgetDao().insertBudget(budget)
    suspend fun updateBudget(budget: BudgetEntity) = db.budgetDao().updateBudget(budget)
    suspend fun deleteBudgetById(id: Long) = db.budgetDao().deleteBudgetById(id)

    fun getPeriodStartAndEnd(period: String): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        when (period.lowercase()) {
            "weekly" -> {
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                cal.add(Calendar.DAY_OF_YEAR, 7)
                val end = cal.timeInMillis - 1
                return Pair(start, end)
            }
            "yearly" -> {
                cal.set(Calendar.DAY_OF_YEAR, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                cal.add(Calendar.YEAR, 1)
                val end = cal.timeInMillis - 1
                return Pair(start, end)
            }
            else -> { // "monthly"
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val start = cal.timeInMillis
                cal.add(Calendar.MONTH, 1)
                val end = cal.timeInMillis - 1
                return Pair(start, end)
            }
        }
    }

    private suspend fun checkBudgetAlerts(transaction: TransactionEntity) {
        if (transaction.type == TransactionType.EXPENSE && !transaction.isTransfer) {
            val budgets = db.budgetDao().getAllBudgets().first()
            val categoryBudget = budgets.find { it.category == transaction.category }
            if (categoryBudget != null) {
                val (startMs, endMs) = getPeriodStartAndEnd(categoryBudget.period)
                val transactions = db.transactionDao().getAllTransactions().first()
                val periodSpent = transactions
                    .filter {
                        it.type == TransactionType.EXPENSE &&
                                !it.isTransfer &&
                                it.category == transaction.category &&
                                it.timestamp in startMs..endMs
                    }
                    .sumOf { it.amount }

                val updatedBudget = categoryBudget.copy(spentAmount = periodSpent)
                db.budgetDao().updateBudget(updatedBudget)

                val ratio = if (categoryBudget.limitAmount > 0) periodSpent / categoryBudget.limitAmount else 0.0
                if (ratio >= 1.0) {
                    db.notificationDao().insertNotification(
                        NotificationEntity(
                            title = "Budget Exceeded!",
                            message = "You have exceeded your ${categoryBudget.period.lowercase()} budget for ${transaction.category.name.lowercase().replaceFirstChar { it.uppercase() }}.",
                            type = NotificationType.BUDGET_ALERT
                        )
                    )
                } else if (ratio >= 0.8) {
                    db.notificationDao().insertNotification(
                        NotificationEntity(
                            title = "Budget Warning",
                            message = "You are at ${(ratio * 100).toInt()}% of your ${categoryBudget.period.lowercase()} budget for ${transaction.category.name.lowercase().replaceFirstChar { it.uppercase() }}.",
                            type = NotificationType.BUDGET_ALERT
                        )
                    )
                }
            }
        }
    }

    // 8. Goals
    suspend fun addGoal(goal: GoalEntity): Long = db.goalDao().insertGoal(goal)
    suspend fun updateGoal(goal: GoalEntity) = db.goalDao().updateGoal(goal)
    suspend fun deleteGoalById(id: Long) = db.goalDao().deleteGoalById(id)

    suspend fun contributeToGoal(goal: GoalEntity, amount: Double, accountId: Long? = null) {
        val newAmount = goal.currentAmount + amount
        val updated = goal.copy(
            currentAmount = newAmount,
            isAchieved = newAmount >= goal.targetAmount
        )
        db.goalDao().updateGoal(updated)

        addTransaction(
            TransactionEntity(
                title = "Tabungan: ${goal.name}",
                amount = amount,
                type = TransactionType.EXPENSE,
                category = TransactionCategory.OTHER,
                note = "Menabung untuk target ${goal.name}",
                accountId = accountId
            )
        )
    }

    // 9. Notifications
    suspend fun markNotificationAsRead(id: Long) = db.notificationDao().markAsRead(id)
    suspend fun markAllNotificationsAsRead() = db.notificationDao().markAllAsRead()

    // 10. CSV Export & Import (Excluding or clearly marking transfers)
    suspend fun exportTransactionsCsv(): String {
        val txs = db.transactionDao().getAllTransactions().first()
        val sb = StringBuilder()
        sb.append("id,title,amount,type,category,timestamp,note,isTransfer,accountId\n")
        txs.forEach { tx ->
            val cleanTitle = tx.title.replace(",", ";").replace("\"", "'")
            val cleanNote = tx.note.replace(",", ";").replace("\"", "'")
            sb.append("${tx.id},\"$cleanTitle\",${tx.amount},${tx.type.name},${tx.category.name},${tx.timestamp},\"$cleanNote\",${tx.isTransfer},${tx.accountId ?: ""}\n")
        }
        return sb.toString()
    }

    suspend fun importTransactionsCsv(csvText: String): Int {
        var count = 0
        val lines = csvText.lines()
        for (i in 1 until lines.size) {
            val line = lines[i].trim()
            if (line.isBlank()) continue
            val parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex())
            if (parts.size >= 5) {
                try {
                    val title = parts[1].replace("\"", "").trim()
                    val amount = parts[2].toDoubleOrNull() ?: 0.0
                    val type = TransactionType.valueOf(parts[3].trim())
                    val category = TransactionCategory.valueOf(parts[4].trim())
                    val timestamp = if (parts.size > 5) parts[5].trim().toLongOrNull() ?: System.currentTimeMillis() else System.currentTimeMillis()
                    val note = if (parts.size > 6) parts[6].replace("\"", "").trim() else ""
                    val isTransfer = if (parts.size > 7) parts[7].trim().toBoolean() else false
                    val accountId = if (parts.size > 8) parts[8].trim().toLongOrNull() else null

                    addTransaction(
                        TransactionEntity(
                            title = title,
                            amount = amount,
                            type = type,
                            category = category,
                            timestamp = timestamp,
                            note = note,
                            isTransfer = isTransfer,
                            accountId = accountId
                        )
                    )
                    count++
                } catch (_: Exception) {}
            }
        }
        return count
    }

    // 11. Complete Full Database Backup & Restore (All 8 Entities)
    suspend fun exportFullBackupJson(): String {
        val root = JSONObject()
        val txs = db.transactionDao().getAllTransactions().first()
        val accounts = db.accountDao().getAllAccounts().first()
        val debts = db.debtDao().getAllDebts().first()
        val debtPayments = db.debtDao().getAllDebtPayments().first()
        val recurring = db.recurringDao().getAllRecurring().first()
        val investments = db.investmentDao().getAllInvestments().first()
        val budgets = db.budgetDao().getAllBudgets().first()
        val goals = db.goalDao().getAllGoals().first()

        root.put("version", 3)
        root.put("exportedAt", System.currentTimeMillis())

        // Transactions
        val txArray = JSONArray()
        txs.forEach {
            val obj = JSONObject()
            obj.put("id", it.id)
            obj.put("title", it.title)
            obj.put("amount", it.amount)
            obj.put("type", it.type.name)
            obj.put("category", it.category.name)
            obj.put("timestamp", it.timestamp)
            obj.put("note", it.note)
            obj.put("accountId", it.accountId ?: JSONObject.NULL)
            obj.put("transferToAccountId", it.transferToAccountId ?: JSONObject.NULL)
            obj.put("isTransfer", it.isTransfer)
            obj.put("isRecurring", it.isRecurring)
            txArray.put(obj)
        }
        root.put("transactions", txArray)

        // Accounts
        val accArray = JSONArray()
        accounts.forEach {
            val obj = JSONObject()
            obj.put("id", it.id)
            obj.put("name", it.name)
            obj.put("type", it.type.name)
            obj.put("balance", it.balance)
            obj.put("accountNumber", it.accountNumber)
            obj.put("institutionName", it.institutionName)
            obj.put("isDefault", it.isDefault)
            obj.put("colorHex", it.colorHex)
            accArray.put(obj)
        }
        root.put("accounts", accArray)

        // Debts
        val debtArray = JSONArray()
        debts.forEach {
            val obj = JSONObject()
            obj.put("id", it.id)
            obj.put("personName", it.personName)
            obj.put("amount", it.amount)
            obj.put("paidAmount", it.paidAmount)
            obj.put("type", it.type.name)
            obj.put("dueDateMillis", it.dueDateMillis)
            obj.put("isSettled", it.isSettled)
            obj.put("notes", it.notes)
            debtArray.put(obj)
        }
        root.put("debts", debtArray)

        // Debt Payments
        val paymentArray = JSONArray()
        debtPayments.forEach {
            val obj = JSONObject()
            obj.put("id", it.id)
            obj.put("debtId", it.debtId)
            obj.put("amount", it.amount)
            obj.put("timestamp", it.timestamp)
            obj.put("note", it.note)
            paymentArray.put(obj)
        }
        root.put("debt_payments", paymentArray)

        // Recurring
        val recArray = JSONArray()
        recurring.forEach {
            val obj = JSONObject()
            obj.put("id", it.id)
            obj.put("title", it.title)
            obj.put("amount", it.amount)
            obj.put("type", it.type.name)
            obj.put("category", it.category.name)
            obj.put("recurringCategory", it.recurringCategory.name)
            obj.put("frequency", it.frequency.name)
            obj.put("nextDueDateMillis", it.nextDueDateMillis)
            obj.put("autoExecute", it.autoExecute)
            obj.put("isActive", it.isActive)
            obj.put("lastExecutedMillis", it.lastExecutedMillis ?: JSONObject.NULL)
            obj.put("note", it.note)
            obj.put("accountId", it.accountId ?: JSONObject.NULL)
            recArray.put(obj)
        }
        root.put("recurring", recArray)

        // Budgets
        val budgetArray = JSONArray()
        budgets.forEach {
            val obj = JSONObject()
            obj.put("id", it.id)
            obj.put("category", it.category.name)
            obj.put("limitAmount", it.limitAmount)
            obj.put("spentAmount", it.spentAmount)
            obj.put("period", it.period)
            budgetArray.put(obj)
        }
        root.put("budgets", budgetArray)

        // Goals
        val goalArray = JSONArray()
        goals.forEach {
            val obj = JSONObject()
            obj.put("id", it.id)
            obj.put("name", it.name)
            obj.put("targetAmount", it.targetAmount)
            obj.put("currentAmount", it.currentAmount)
            obj.put("targetDateMillis", it.targetDateMillis)
            obj.put("category", it.category)
            obj.put("isAchieved", it.isAchieved)
            goalArray.put(obj)
        }
        root.put("goals", goalArray)

        // Investments
        val invArray = JSONArray()
        investments.forEach {
            val obj = JSONObject()
            obj.put("id", it.id)
            obj.put("assetName", it.assetName)
            obj.put("symbol", it.symbol)
            obj.put("quantity", it.quantity)
            obj.put("buyPrice", it.buyPrice)
            obj.put("currentPrice", it.currentPrice)
            obj.put("dividendReceived", it.dividendReceived)
            obj.put("type", it.type.name)
            obj.put("updatedAtMillis", it.updatedAtMillis)
            obj.put("notes", it.notes)
            invArray.put(obj)
        }
        root.put("investments", invArray)

        return root.toString(2)
    }

    suspend fun restoreFullBackupJson(jsonString: String): Boolean {
        return try {
            val root = JSONObject(jsonString)

            // 1. Clear existing database
            db.transactionDao().deleteAllTransactions()
            db.accountDao().deleteAllAccounts()
            db.debtDao().deleteAllDebts()
            db.debtDao().deleteAllDebtPayments()
            db.recurringDao().deleteAllRecurring()
            db.budgetDao().deleteAllBudgets()
            db.goalDao().deleteAllGoals()
            db.investmentDao().deleteAllInvestments()

            // 2. Restore Accounts
            if (root.has("accounts")) {
                val accArray = root.getJSONArray("accounts")
                for (i in 0 until accArray.length()) {
                    val obj = accArray.getJSONObject(i)
                    db.accountDao().insertAccount(
                        AccountEntity(
                            id = obj.optLong("id", 0),
                            name = obj.getString("name"),
                            type = AccountType.valueOf(obj.getString("type")),
                            balance = obj.getDouble("balance"),
                            accountNumber = obj.optString("accountNumber", ""),
                            institutionName = obj.optString("institutionName", ""),
                            isDefault = obj.optBoolean("isDefault", false),
                            colorHex = obj.optString("colorHex", "#FFFFFF")
                        )
                    )
                }
            }

            // 3. Restore Transactions
            if (root.has("transactions")) {
                val txArray = root.getJSONArray("transactions")
                for (i in 0 until txArray.length()) {
                    val obj = txArray.getJSONObject(i)
                    db.transactionDao().insertTransaction(
                        TransactionEntity(
                            id = obj.optLong("id", 0),
                            title = obj.getString("title"),
                            amount = obj.getDouble("amount"),
                            type = TransactionType.valueOf(obj.getString("type")),
                            category = TransactionCategory.valueOf(obj.getString("category")),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                            note = obj.optString("note", ""),
                            accountId = if (obj.isNull("accountId")) null else obj.getLong("accountId"),
                            transferToAccountId = if (obj.isNull("transferToAccountId")) null else obj.getLong("transferToAccountId"),
                            isTransfer = obj.optBoolean("isTransfer", false),
                            isRecurring = obj.optBoolean("isRecurring", false)
                        )
                    )
                }
            }

            // 4. Restore Debts
            if (root.has("debts")) {
                val debtArray = root.getJSONArray("debts")
                for (i in 0 until debtArray.length()) {
                    val obj = debtArray.getJSONObject(i)
                    db.debtDao().insertDebt(
                        DebtEntity(
                            id = obj.optLong("id", 0),
                            personName = obj.getString("personName"),
                            amount = obj.getDouble("amount"),
                            paidAmount = obj.optDouble("paidAmount", 0.0),
                            type = DebtType.valueOf(obj.getString("type")),
                            dueDateMillis = obj.optLong("dueDateMillis", System.currentTimeMillis()),
                            isSettled = obj.optBoolean("isSettled", false),
                            notes = obj.optString("notes", "")
                        )
                    )
                }
            }

            // 5. Restore Debt Payments
            if (root.has("debt_payments")) {
                val pArray = root.getJSONArray("debt_payments")
                for (i in 0 until pArray.length()) {
                    val obj = pArray.getJSONObject(i)
                    db.debtDao().insertPayment(
                        DebtPaymentEntity(
                            id = obj.optLong("id", 0),
                            debtId = obj.getLong("debtId"),
                            amount = obj.getDouble("amount"),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                            note = obj.optString("note", "")
                        )
                    )
                }
            }

            // 6. Restore Recurring
            if (root.has("recurring")) {
                val recArray = root.getJSONArray("recurring")
                for (i in 0 until recArray.length()) {
                    val obj = recArray.getJSONObject(i)
                    db.recurringDao().insertRecurring(
                        RecurringEntity(
                            id = obj.optLong("id", 0),
                            title = obj.getString("title"),
                            amount = obj.getDouble("amount"),
                            type = TransactionType.valueOf(obj.getString("type")),
                            category = TransactionCategory.valueOf(obj.getString("category")),
                            recurringCategory = RecurringCategoryType.valueOf(obj.optString("recurringCategory", "SUBSCRIPTION")),
                            frequency = RecurringFrequency.valueOf(obj.optString("frequency", "MONTHLY")),
                            nextDueDateMillis = obj.optLong("nextDueDateMillis", System.currentTimeMillis()),
                            autoExecute = obj.optBoolean("autoExecute", false),
                            isActive = obj.optBoolean("isActive", true),
                            lastExecutedMillis = if (obj.isNull("lastExecutedMillis")) null else obj.getLong("lastExecutedMillis"),
                            note = obj.optString("note", ""),
                            accountId = if (obj.isNull("accountId")) null else obj.getLong("accountId")
                        )
                    )
                }
            }

            // 7. Restore Budgets
            if (root.has("budgets")) {
                val bArray = root.getJSONArray("budgets")
                for (i in 0 until bArray.length()) {
                    val obj = bArray.getJSONObject(i)
                    db.budgetDao().insertBudget(
                        BudgetEntity(
                            id = obj.optLong("id", 0),
                            category = TransactionCategory.valueOf(obj.getString("category")),
                            limitAmount = obj.getDouble("limitAmount"),
                            spentAmount = obj.optDouble("spentAmount", 0.0),
                            period = obj.optString("period", "Monthly")
                        )
                    )
                }
            }

            // 8. Restore Goals
            if (root.has("goals")) {
                val gArray = root.getJSONArray("goals")
                for (i in 0 until gArray.length()) {
                    val obj = gArray.getJSONObject(i)
                    db.goalDao().insertGoal(
                        GoalEntity(
                            id = obj.optLong("id", 0),
                            name = obj.getString("name"),
                            targetAmount = obj.getDouble("targetAmount"),
                            currentAmount = obj.optDouble("currentAmount", 0.0),
                            targetDateMillis = obj.optLong("targetDateMillis", System.currentTimeMillis()),
                            category = obj.optString("category", "General"),
                            isAchieved = obj.optBoolean("isAchieved", false)
                        )
                    )
                }
            }

            // 9. Restore Investments
            if (root.has("investments")) {
                val invArray = root.getJSONArray("investments")
                for (i in 0 until invArray.length()) {
                    val obj = invArray.getJSONObject(i)
                    db.investmentDao().insertInvestment(
                        InvestmentEntity(
                            id = obj.optLong("id", 0),
                            assetName = obj.getString("assetName"),
                            symbol = obj.getString("symbol"),
                            quantity = obj.getDouble("quantity"),
                            buyPrice = obj.getDouble("buyPrice"),
                            currentPrice = obj.getDouble("currentPrice"),
                            dividendReceived = obj.optDouble("dividendReceived", 0.0),
                            type = InvestmentType.valueOf(obj.optString("type", "STOCKS")),
                            updatedAtMillis = obj.optLong("updatedAtMillis", System.currentTimeMillis()),
                            notes = obj.optString("notes", "")
                        )
                    )
                }
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // 12. Complete Initial Seed Data
    suspend fun seedInitialDataIfEmpty() {
        val existingTransactions = db.transactionDao().getAllTransactions().first()
        if (existingTransactions.isNotEmpty()) return

        val now = System.currentTimeMillis()
        val dayMs = 24 * 60 * 60 * 1000L

        // Accounts: Cash, Bank, E-Wallet
        val bca = AccountEntity(name = "BCA Main Account", type = AccountType.BANK, balance = 18500.0, accountNumber = "•8829", institutionName = "Bank Central Asia", isDefault = true)
        val mandiri = AccountEntity(name = "Mandiri Payroll", type = AccountType.BANK, balance = 6200.0, accountNumber = "•3104", institutionName = "Bank Mandiri")
        val cash = AccountEntity(name = "Dompet Fisik", type = AccountType.CASH, balance = 850.0, accountNumber = "Cash", institutionName = "Cash Pocket")
        val gopay = AccountEntity(name = "GoPay / OVO", type = AccountType.EWALLET, balance = 450.0, accountNumber = "0812•••", institutionName = "E-Wallet Digital")

        val bcaId = db.accountDao().insertAccount(bca)
        val mandiriId = db.accountDao().insertAccount(mandiri)
        val cashId = db.accountDao().insertAccount(cash)
        val gopayId = db.accountDao().insertAccount(gopay)

        // Transactions
        val sampleTransactions = listOf(
            TransactionEntity(title = "Monthly Tech Salary", amount = 8500.00, type = TransactionType.INCOME, category = TransactionCategory.SALARY, timestamp = now - 1 * dayMs, note = "Tech Director Salary", accountId = bcaId),
            TransactionEntity(title = "Apple Store Hardware", amount = 1299.00, type = TransactionType.EXPENSE, category = TransactionCategory.SHOPPING, timestamp = now - 2 * dayMs, note = "MacBook Air purchase", accountId = bcaId),
            TransactionEntity(title = "Whole Foods Organic Market", amount = 184.50, type = TransactionType.EXPENSE, category = TransactionCategory.FOOD, timestamp = now - 3 * dayMs, note = "Weekly groceries", accountId = gopayId),
            TransactionEntity(title = "Stock Investment Dividend", amount = 420.00, type = TransactionType.INCOME, category = TransactionCategory.INVESTMENT, timestamp = now - 6 * dayMs, note = "S&P 500 quarterly payout", accountId = mandiriId),
            TransactionEntity(title = "Electric & Utilities", amount = 145.20, type = TransactionType.EXPENSE, category = TransactionCategory.UTILITIES, timestamp = now - 8 * dayMs, note = "Power & high-speed fiber", accountId = mandiriId),
            TransactionEntity(title = "Equinox Gym Membership", amount = 260.00, type = TransactionType.EXPENSE, category = TransactionCategory.HEALTH, timestamp = now - 10 * dayMs, note = "Monthly fitness subscription", accountId = bcaId),
            TransactionEntity(title = "Linear & Figma Pro", amount = 45.00, type = TransactionType.EXPENSE, category = TransactionCategory.ENTERTAINMENT, timestamp = now - 12 * dayMs, note = "Software suite subscriptions", accountId = gopayId)
        )
        sampleTransactions.forEach { db.transactionDao().insertTransaction(it) }

        // Budgets
        val sampleBudgets = listOf(
            BudgetEntity(category = TransactionCategory.FOOD, limitAmount = 800.0, spentAmount = 184.50, period = "Monthly"),
            BudgetEntity(category = TransactionCategory.SHOPPING, limitAmount = 1500.0, spentAmount = 1299.00, period = "Monthly"),
            BudgetEntity(category = TransactionCategory.ENTERTAINMENT, limitAmount = 300.0, spentAmount = 45.00, period = "Monthly"),
            BudgetEntity(category = TransactionCategory.UTILITIES, limitAmount = 250.0, spentAmount = 145.20, period = "Monthly")
        )
        sampleBudgets.forEach { db.budgetDao().insertBudget(it) }

        // Goals
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, 6)
        val sampleGoals = listOf(
            GoalEntity(name = "Emergency Fund", targetAmount = 20000.0, currentAmount = 14500.0, targetDateMillis = cal.timeInMillis, category = "Safety"),
            GoalEntity(name = "Japan Spring Trip", targetAmount = 6000.0, currentAmount = 4200.0, targetDateMillis = cal.timeInMillis, category = "Travel"),
            GoalEntity(name = "Angel Investment Fund", targetAmount = 15000.0, currentAmount = 5000.0, targetDateMillis = cal.timeInMillis, category = "Investing")
        )
        sampleGoals.forEach { db.goalDao().insertGoal(it) }

        // Hutang & Piutang (Debts & Receivables)
        val sampleDebts = listOf(
            DebtEntity(personName = "Alex Wijaya (Laptop)", amount = 2500.0, paidAmount = 1500.0, type = DebtType.PIUTANG, dueDateMillis = now + 15 * dayMs, isSettled = false, notes = "Pinjam untuk beli laptop kerja"),
            DebtEntity(personName = "Om Budi (Pinjaman Usaha)", amount = 5000.0, paidAmount = 2000.0, type = DebtType.HUTANG, dueDateMillis = now + 30 * dayMs, isSettled = false, notes = "Modal awal workshop"),
            DebtEntity(personName = "Sarah - Dinner Split", amount = 120.0, paidAmount = 120.0, type = DebtType.PIUTANG, dueDateMillis = now - 2 * dayMs, isSettled = true, notes = "Reimburse sushi dinner")
        )
        sampleDebts.forEach {
            val dId = db.debtDao().insertDebt(it)
            if (it.paidAmount > 0) {
                db.debtDao().insertPayment(DebtPaymentEntity(debtId = dId, amount = it.paidAmount, timestamp = now - 5 * dayMs, note = "Pembayaran bertahap"))
            }
        }

        // Recurring (Gaji, Uang bulanan, Tagihan, Subscription)
        val sampleRecurring = listOf(
            RecurringEntity(title = "Gaji Bulanan Tech", amount = 8500.0, type = TransactionType.INCOME, category = TransactionCategory.SALARY, recurringCategory = RecurringCategoryType.GAJI, frequency = RecurringFrequency.MONTHLY, nextDueDateMillis = now + 8 * dayMs, autoExecute = true, note = "Otomatis cair tanggal 25", accountId = bcaId),
            RecurringEntity(title = "Uang Bulanan Orang Tua", amount = 1000.0, type = TransactionType.EXPENSE, category = TransactionCategory.OTHER, recurringCategory = RecurringCategoryType.UANG_BULANAN, frequency = RecurringFrequency.MONTHLY, nextDueDateMillis = now + 5 * dayMs, autoExecute = false, note = "Kirim ke rekening ibu", accountId = mandiriId),
            RecurringEntity(title = "Tagihan Listrik & Fiber PLN", amount = 145.0, type = TransactionType.EXPENSE, category = TransactionCategory.UTILITIES, recurringCategory = RecurringCategoryType.TAGIHAN, frequency = RecurringFrequency.MONTHLY, nextDueDateMillis = now + 12 * dayMs, autoExecute = true, note = "Auto-debit PLN & Indihome", accountId = mandiriId),
            RecurringEntity(title = "Netflix & Spotify Premium", amount = 35.0, type = TransactionType.EXPENSE, category = TransactionCategory.ENTERTAINMENT, recurringCategory = RecurringCategoryType.SUBSCRIPTION, frequency = RecurringFrequency.MONTHLY, nextDueDateMillis = now + 18 * dayMs, autoExecute = true, note = "Family plan entertainment", accountId = gopayId)
        )
        sampleRecurring.forEach { db.recurringDao().insertRecurring(it) }

        // Investments (Saham, Kripto, Reksadana, SBN)
        val sampleInvestments = listOf(
            InvestmentEntity(assetName = "Bank Central Asia", symbol = "BBCA", quantity = 50.0, buyPrice = 8800.0, currentPrice = 10200.0, dividendReceived = 420.0, type = InvestmentType.STOCK, notes = "Blue chip perbankan dividend aristocrat"),
            InvestmentEntity(assetName = "Vanguard S&P 500 ETF", symbol = "VOO", quantity = 15.0, buyPrice = 420.0, currentPrice = 512.0, dividendReceived = 185.0, type = InvestmentType.MUTUAL_FUND, notes = "Core US index portfolio"),
            InvestmentEntity(assetName = "Bitcoin", symbol = "BTC", quantity = 0.25, buyPrice = 54000.0, currentPrice = 64500.0, dividendReceived = 0.0, type = InvestmentType.CRYPTO, notes = "Digital gold store of value"),
            InvestmentEntity(assetName = "Surat Berharga Negara ORI025", symbol = "ORI025", quantity = 10.0, buyPrice = 1000.0, currentPrice = 1000.0, dividendReceived = 62.5, type = InvestmentType.BOND, notes = "Kupon fix 6.25% p.a. guaranteed")
        )
        sampleInvestments.forEach { db.investmentDao().insertInvestment(it) }

        // Notifications
        val sampleNotifications = listOf(
            NotificationEntity(title = "Welcome to Kai Finance", message = "Semua fitur finansial profesional: Rekening, Hutang & Piutang, Transaksi Rutin, Investasi, Budget, dan AI siap digunakan.", timestamp = now, type = NotificationType.SYSTEM),
            NotificationEntity(title = "Jatuh Tempo Piutang", message = "Alex Wijaya memiliki sisa piutang $1,000 jatuh tempo 15 hari lagi.", timestamp = now - dayMs, type = NotificationType.BILL_REMINDER),
            NotificationEntity(title = "Dividen Diterima", message = "Dividen BBCA sebesar $420 telah dicatat ke portofolio.", timestamp = now - 2 * dayMs, type = NotificationType.SAVINGS_MILESTONE)
        )
        sampleNotifications.forEach { db.notificationDao().insertNotification(it) }
    }
}
