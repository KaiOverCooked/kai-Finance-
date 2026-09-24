package com.example.data.integrity

import com.example.data.local.KaiDatabase
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.DebtEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.TransactionType
import kotlinx.coroutines.flow.first
import kotlin.math.abs

data class AccountBalanceIssue(
    val account: AccountEntity,
    val recordedBalance: Double,
    val calculatedBalance: Double,
    val difference: Double
) {
    val isMismatch: Boolean = abs(difference) > 0.01
}

data class DebtIssue(
    val debt: DebtEntity,
    val recordedPaid: Double,
    val actualPaymentsSum: Double,
    val difference: Double
) {
    val isMismatch: Boolean = abs(difference) > 0.01
}

data class BudgetIssue(
    val budget: BudgetEntity,
    val recordedSpent: Double,
    val actualSpent: Double,
    val difference: Double
) {
    val isMismatch: Boolean = abs(difference) > 0.01
}

data class DataIntegrityReport(
    val accountIssues: List<AccountBalanceIssue>,
    val orphanedTransactions: List<TransactionEntity>,
    val debtIssues: List<DebtIssue>,
    val budgetIssues: List<BudgetIssue>,
    val totalAccountsChecked: Int,
    val totalTransactionsChecked: Int,
    val checkedAt: Long = System.currentTimeMillis()
) {
    val isHealthy: Boolean = accountIssues.none { it.isMismatch } &&
            orphanedTransactions.isEmpty() &&
            debtIssues.none { it.isMismatch }

    val healthyScorePercentage: Int = run {
        var score = 100
        val accountMismatchCount = accountIssues.count { it.isMismatch }
        score -= (accountMismatchCount * 20)
        score -= (orphanedTransactions.size * 15)
        score -= (debtIssues.count { it.isMismatch } * 10)
        score.coerceIn(0, 100)
    }
}

class DataIntegrityChecker(private val db: KaiDatabase) {

    suspend fun runAudit(): DataIntegrityReport {
        val accounts = db.accountDao().getAllAccounts().first()
        val allTransactions = db.transactionDao().getAllTransactions().first()
        val debts = db.debtDao().getAllDebts().first()
        val allPayments = db.debtDao().getAllDebtPayments().first()
        val budgets = db.budgetDao().getAllBudgets().first()

        val accountIds = accounts.map { it.id }.toSet()

        // 1. Check Account Balance Consistency
        val accountIssues = accounts.map { account ->
            val linkedTxs = allTransactions.filter { it.accountId == account.id }
            val sumIncome = linkedTxs.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val sumExpense = linkedTxs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            val calculatedBalance = account.initialBalance + sumIncome - sumExpense
            val diff = account.balance - calculatedBalance

            AccountBalanceIssue(
                account = account,
                recordedBalance = account.balance,
                calculatedBalance = calculatedBalance,
                difference = diff
            )
        }

        // 2. Check Orphaned Transactions
        val orphaned = allTransactions.filter { tx ->
            tx.accountId != null && !accountIds.contains(tx.accountId)
        }

        // 3. Check Debt Payments Consistency
        val debtIssues = debts.map { debt ->
            val paymentsSum = allPayments.filter { it.debtId == debt.id }.sumOf { it.amount }
            DebtIssue(
                debt = debt,
                recordedPaid = debt.paidAmount,
                actualPaymentsSum = paymentsSum,
                difference = debt.paidAmount - paymentsSum
            )
        }

        // 4. Check Budget Consistency
        val now = System.currentTimeMillis()
        val oneMonthAgo = now - 30L * 24 * 60 * 60 * 1000L
        val budgetIssues = budgets.map { budget ->
            val actualSpent = allTransactions
                .filter { it.category == budget.category && it.type == TransactionType.EXPENSE && it.timestamp >= oneMonthAgo && !it.isTransfer }
                .sumOf { it.amount }
            BudgetIssue(
                budget = budget,
                recordedSpent = budget.spentAmount,
                actualSpent = actualSpent,
                difference = budget.spentAmount - actualSpent
            )
        }

        return DataIntegrityReport(
            accountIssues = accountIssues,
            orphanedTransactions = orphaned,
            debtIssues = debtIssues,
            budgetIssues = budgetIssues,
            totalAccountsChecked = accounts.size,
            totalTransactionsChecked = allTransactions.size
        )
    }

    suspend fun reconcileAccountBalance(accountId: Long): Boolean {
        val account = db.accountDao().getAccountById(accountId) ?: return false
        val allTransactions = db.transactionDao().getAllTransactions().first()
        val linkedTxs = allTransactions.filter { it.accountId == account.id }
        val sumIncome = linkedTxs.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val sumExpense = linkedTxs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val calculatedBalance = account.initialBalance + sumIncome - sumExpense

        db.accountDao().updateAccount(account.copy(balance = calculatedBalance))
        return true
    }

    suspend fun reconcileAll(): Int {
        val report = runAudit()
        var fixedCount = 0

        // Fix account mismatches
        for (issue in report.accountIssues) {
            if (issue.isMismatch) {
                db.accountDao().updateAccount(issue.account.copy(balance = issue.calculatedBalance))
                fixedCount++
            }
        }

        // Fix debt payment mismatches
        for (debtIssue in report.debtIssues) {
            if (debtIssue.isMismatch) {
                val isSettled = debtIssue.actualPaymentsSum >= debtIssue.debt.amount
                db.debtDao().updateDebt(
                    debtIssue.debt.copy(
                        paidAmount = debtIssue.actualPaymentsSum,
                        isSettled = isSettled
                    )
                )
                fixedCount++
            }
        }

        return fixedCount
    }
}
