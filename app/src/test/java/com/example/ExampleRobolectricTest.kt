package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.KaiDatabase
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.AccountType
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.DebtEntity
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
import com.example.data.repository.FinanceRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Calendar

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var database: KaiDatabase
    private lateinit var repository: FinanceRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, KaiDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = FinanceRepository(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testAppName() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Kai Finance", appName)
    }

    // 1. TEST SALDO: Tambah, Edit, Hapus Transaksi
    @Test
    fun testSaldoAddEditDeleteTransaction() = runBlocking {
        val acc1Id = repository.addAccount(
            AccountEntity(name = "Bank BCA", type = AccountType.BANK, balance = 1000000.0)
        )
        val acc2Id = repository.addAccount(
            AccountEntity(name = "GoPay", type = AccountType.EWALLET, balance = 200000.0)
        )

        // Tambah Income 500.000 ke BCA -> saldo BCA jadi 1.500.000
        val incomeTxId = repository.addTransaction(
            TransactionEntity(
                title = "Gaji Bonus",
                amount = 500000.0,
                type = TransactionType.INCOME,
                category = TransactionCategory.SALARY,
                accountId = acc1Id
            )
        )
        assertEquals(1500000.0, repository.getAccountById(acc1Id)!!.balance, 0.01)

        // Tambah Expense 200.000 dari BCA -> saldo BCA jadi 1.300.000
        val expenseTxId = repository.addTransaction(
            TransactionEntity(
                title = "Belanja Mingguan",
                amount = 200000.0,
                type = TransactionType.EXPENSE,
                category = TransactionCategory.FOOD,
                accountId = acc1Id
            )
        )
        assertEquals(1300000.0, repository.getAccountById(acc1Id)!!.balance, 0.01)

        // Edit Expense dari 200.000 menjadi 300.000 -> saldo BCA berkurang 100.000 lagi jadi 1.200.000
        val currentExpTx = database.transactionDao().getTransactionById(expenseTxId)!!
        repository.updateTransaction(currentExpTx.copy(amount = 300000.0))
        assertEquals(1200000.0, repository.getAccountById(acc1Id)!!.balance, 0.01)

        // Edit pindah akun dari BCA ke GoPay -> BCA kembali +300.000 (1.500.000), GoPay -300.000 (-100.000)
        val updatedExpTx = database.transactionDao().getTransactionById(expenseTxId)!!
        repository.updateTransaction(updatedExpTx.copy(accountId = acc2Id))
        assertEquals(1500000.0, repository.getAccountById(acc1Id)!!.balance, 0.01)
        assertEquals(-100000.0, repository.getAccountById(acc2Id)!!.balance, 0.01)

        // Hapus income 500.000 dari BCA -> saldo BCA kembali ke saldo awal 1.000.000
        repository.deleteTransactionById(incomeTxId)
        assertEquals(1000000.0, repository.getAccountById(acc1Id)!!.balance, 0.01)
    }

    // 2. TEST SALDO: Transfer Antar Akun
    @Test
    fun testSaldoTransferBetweenAccounts() = runBlocking {
        val acc1Id = repository.addAccount(
            AccountEntity(name = "Bank BCA", type = AccountType.BANK, balance = 2000000.0)
        )
        val acc2Id = repository.addAccount(
            AccountEntity(name = "GoPay", type = AccountType.EWALLET, balance = 500000.0)
        )

        val success = repository.transferBetweenAccounts(acc1Id, acc2Id, 700000.0, "Top up GoPay")
        assertTrue(success)

        val acc1 = repository.getAccountById(acc1Id)!!
        val acc2 = repository.getAccountById(acc2Id)!!
        assertEquals(1300000.0, acc1.balance, 0.01)
        assertEquals(1200000.0, acc2.balance, 0.01)

        // Cek bahwa transaksi transfer bertanda isTransfer = true
        val txs = repository.allTransactions.first()
        assertEquals(2, txs.size)
        assertTrue(txs.all { it.isTransfer })
    }

    // 3. TEST SALDO: Hutang & Pembayaran
    @Test
    fun testSaldoDebtAndPayment() = runBlocking {
        val accId = repository.addAccount(
            AccountEntity(name = "Kas Utama", type = AccountType.CASH, balance = 1000000.0)
        )

        // Catat hutang ke Pak Budi (1.000.000)
        val debtId = repository.addDebt(
            DebtEntity(
                personName = "Pak Budi",
                amount = 1000000.0,
                type = DebtType.HUTANG,
                dueDateMillis = System.currentTimeMillis() + 86400000L
            )
        )

        // Bayar cicilan hutang 400.000 dari Kas Utama
        repository.recordDebtPayment(debtId, 400000.0, "Cicilan 1", accountId = accId)

        // Saldo Kas Utama berkurang jadi 600.000
        val acc = repository.getAccountById(accId)!!
        assertEquals(600000.0, acc.balance, 0.01)

        // Sisa hutang berkurang
        val debt = database.debtDao().getDebtById(debtId)!!
        assertEquals(400000.0, debt.paidAmount, 0.01)
        assertEquals(600000.0, debt.amount - debt.paidAmount, 0.01)
        assertEquals(false, debt.isSettled)

        // Catat piutang dari Siti (500.000)
        val piutangId = repository.addDebt(
            DebtEntity(
                personName = "Siti",
                amount = 500000.0,
                type = DebtType.PIUTANG,
                dueDateMillis = System.currentTimeMillis() + 86400000L
            )
        )

        // Siti melunasi 500.000 masuk ke Kas Utama
        repository.recordDebtPayment(piutangId, 500000.0, "Pelunasan Siti", accountId = accId)

        // Saldo Kas Utama bertambah 500.000 menjadi 1.100.000
        val accAfterPiutang = repository.getAccountById(accId)!!
        assertEquals(1100000.0, accAfterPiutang.balance, 0.01)

        val piutang = database.debtDao().getDebtById(piutangId)!!
        assertEquals(500000.0, piutang.paidAmount, 0.01)
        assertEquals(true, piutang.isSettled)
    }

    // 4. TEST RECURRING: Otomatisasi & Maju Tanggal
    @Test
    fun testRecurringAdvanceDateCalculation() = runBlocking {
        val cal = Calendar.getInstance()
        cal.set(2026, Calendar.JANUARY, 1, 10, 0, 0)
        val initialDue = cal.timeInMillis

        val nextMonthly = repository.calculateNextDueDate(initialDue, RecurringFrequency.MONTHLY)
        val resultCal = Calendar.getInstance().apply { timeInMillis = nextMonthly }
        assertEquals(Calendar.FEBRUARY, resultCal.get(Calendar.MONTH))
        assertEquals(2026, resultCal.get(Calendar.YEAR))

        val nextWeekly = repository.calculateNextDueDate(initialDue, RecurringFrequency.WEEKLY)
        val weekCal = Calendar.getInstance().apply { timeInMillis = nextWeekly }
        assertEquals(8, weekCal.get(Calendar.DAY_OF_MONTH))

        // Eksekusi recurring item otomatis memajukan tanggal ke masa depan
        val futureCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 2) }
        val recId = repository.addRecurring(
            RecurringEntity(
                title = "Monthly WiFi",
                amount = 350000.0,
                type = TransactionType.EXPENSE,
                category = TransactionCategory.UTILITIES,
                recurringCategory = RecurringCategoryType.SUBSCRIPTION,
                frequency = RecurringFrequency.MONTHLY,
                nextDueDateMillis = futureCal.timeInMillis,
                autoExecute = true
            )
        )

        val execSuccess = repository.executeRecurringItem(recId)
        assertTrue(execSuccess)

        val updated = repository.allRecurring.first().find { it.id == recId }
        assertNotNull(updated)
        assertTrue(updated!!.nextDueDateMillis > futureCal.timeInMillis)
    }

    // 5. TEST BUDGET: Periode dan Peringatan Alert Akurat
    @Test
    fun testBudgetPeriodAndAlert() = runBlocking {
        val budgetId = repository.addBudget(
            BudgetEntity(
                category = TransactionCategory.FOOD,
                limitAmount = 1000000.0,
                spentAmount = 0.0,
                period = "Monthly"
            )
        )

        // Catat pengeluaran Makanan 850.000 (85% - masuk ambang batas peringatan >= 80%)
        repository.addTransaction(
            TransactionEntity(
                title = "Restoran Keluarga",
                amount = 850000.0,
                type = TransactionType.EXPENSE,
                category = TransactionCategory.FOOD,
                timestamp = System.currentTimeMillis()
            )
        )

        val updatedBudget = database.budgetDao().getAllBudgets().first().find { it.id == budgetId }!!
        assertEquals(850000.0, updatedBudget.spentAmount, 0.01)

        val notifications = repository.allNotifications.first()
        assertTrue(notifications.any { it.type == NotificationType.BUDGET_ALERT && it.title == "Budget Warning" })

        // Catat pengeluaran tambahan 200.000 (total 1.050.000 - melebihi limit 1.000.000)
        repository.addTransaction(
            TransactionEntity(
                title = "Snack Kafe",
                amount = 200000.0,
                type = TransactionType.EXPENSE,
                category = TransactionCategory.FOOD,
                timestamp = System.currentTimeMillis()
            )
        )

        val exceededNotifications = repository.allNotifications.first()
        assertTrue(exceededNotifications.any { it.type == NotificationType.BUDGET_ALERT && it.title == "Budget Exceeded!" })
    }

    // 6. TEST RESTORE: Export 100% -> Hapus Data -> Import -> Semua Data Balik Utuh
    @Test
    fun testExportDeleteRestoreAllEntities() = runBlocking {
        // 1. Masukkan semua entity lengkap: Accounts, Transactions, Debts, Payments, Recurring, Budgets, Goals, Investments, Notifications
        val accId = repository.addAccount(AccountEntity(name = "Bank Jago", type = AccountType.BANK, balance = 7500000.0))
        repository.addTransaction(
            TransactionEntity(
                title = "Dividen Saham",
                amount = 450000.0,
                type = TransactionType.INCOME,
                category = TransactionCategory.INVESTMENT,
                accountId = accId
            )
        )
        val debtId = repository.addDebt(
            DebtEntity(
                personName = "Andi Wijaya",
                amount = 2000000.0,
                paidAmount = 500000.0,
                type = DebtType.PIUTANG,
                dueDateMillis = System.currentTimeMillis() + 86400000L
            )
        )
        database.debtDao().insertPayment(
            com.example.data.local.entity.DebtPaymentEntity(debtId = debtId, amount = 500000.0, note = "DP Cicilan")
        )
        repository.addRecurring(
            RecurringEntity(
                title = "Spotify Family",
                amount = 86000.0,
                type = TransactionType.EXPENSE,
                category = TransactionCategory.ENTERTAINMENT,
                recurringCategory = RecurringCategoryType.SUBSCRIPTION,
                frequency = RecurringFrequency.MONTHLY,
                nextDueDateMillis = System.currentTimeMillis() + 864000000L
            )
        )
        repository.addBudget(BudgetEntity(category = TransactionCategory.ENTERTAINMENT, limitAmount = 500000.0, spentAmount = 86000.0, period = "Monthly"))
        repository.addGoal(GoalEntity(name = "Dana Darurat", targetAmount = 50000000.0, currentAmount = 25000000.0, targetDateMillis = System.currentTimeMillis() + 10000000000L))
        repository.addInvestment(
            InvestmentEntity(
                assetName = "Bank Central Asia",
                symbol = "BBCA",
                quantity = 100.0,
                buyPrice = 9000.0,
                currentPrice = 10500.0,
                dividendReceived = 450000.0,
                type = InvestmentType.STOCK
            )
        )
        database.notificationDao().insertNotification(
            NotificationEntity(
                title = "Pengingat Sistem",
                message = "Backup berkala telah dijadwalkan.",
                type = NotificationType.SYSTEM
            )
        )

        // 2. Export Full JSON Backup
        val backupJson = repository.exportFullBackupJson()
        assertTrue(backupJson.contains("Bank Jago"))
        assertTrue(backupJson.contains("Dividen Saham"))
        assertTrue(backupJson.contains("Andi Wijaya"))
        assertTrue(backupJson.contains("Spotify Family"))
        assertTrue(backupJson.contains("Dana Darurat"))
        assertTrue(backupJson.contains("BBCA"))
        assertTrue(backupJson.contains("Pengingat Sistem"))

        // 3. Hapus seluruh data di database
        database.accountDao().deleteAllAccounts()
        database.transactionDao().deleteAllTransactions()
        database.debtDao().deleteAllDebts()
        database.debtDao().deleteAllDebtPayments()
        database.recurringDao().deleteAllRecurring()
        database.budgetDao().deleteAllBudgets()
        database.goalDao().deleteAllGoals()
        database.investmentDao().deleteAllInvestments()
        database.notificationDao().deleteAllNotifications()

        // Pastikan database benar-benar kosong
        assertEquals(0, repository.allAccounts.first().size)
        assertEquals(0, repository.allTransactions.first().size)
        assertEquals(0, repository.allDebts.first().size)
        assertEquals(0, repository.allRecurring.first().size)
        assertEquals(0, repository.allBudgets.first().size)
        assertEquals(0, repository.allGoals.first().size)
        assertEquals(0, repository.allInvestments.first().size)
        assertEquals(0, repository.allNotifications.first().size)

        // 4. Restore database dari JSON
        val restoreSuccess = repository.restoreFullBackupJson(backupJson)
        assertTrue(restoreSuccess)

        // 5. Verifikasi semua data kembali utuh!
        val restoredAccounts = repository.allAccounts.first()
        val restoredTransactions = repository.allTransactions.first()
        val restoredDebts = repository.allDebts.first()
        val restoredRecurring = repository.allRecurring.first()
        val restoredBudgets = repository.allBudgets.first()
        val restoredGoals = repository.allGoals.first()
        val restoredInvestments = repository.allInvestments.first()
        val restoredNotifications = repository.allNotifications.first()

        assertEquals(1, restoredAccounts.size)
        assertEquals("Bank Jago", restoredAccounts[0].name)
        assertEquals(7950000.0, restoredAccounts[0].balance, 0.01)

        assertEquals(1, restoredTransactions.size)
        assertEquals("Dividen Saham", restoredTransactions[0].title)

        assertEquals(1, restoredDebts.size)
        assertEquals("Andi Wijaya", restoredDebts[0].personName)

        val restoredPayments = repository.getPaymentsForDebt(restoredDebts[0].id).first()
        assertEquals(1, restoredPayments.size)
        assertEquals(500000.0, restoredPayments[0].amount, 0.01)

        assertEquals(1, restoredRecurring.size)
        assertEquals("Spotify Family", restoredRecurring[0].title)

        assertEquals(1, restoredBudgets.size)
        assertEquals(TransactionCategory.ENTERTAINMENT, restoredBudgets[0].category)

        assertEquals(1, restoredGoals.size)
        assertEquals("Dana Darurat", restoredGoals[0].name)

        assertEquals(1, restoredInvestments.size)
        assertEquals("BBCA", restoredInvestments[0].symbol)

        assertEquals(1, restoredNotifications.size)
        assertEquals("Pengingat Sistem", restoredNotifications[0].title)
    }
}
