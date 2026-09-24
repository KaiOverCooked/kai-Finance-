package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.KaiDatabase
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.AccountType
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

    @Test
    fun testTransferDoesNotAffectIncomeOrExpense() = runBlocking {
        // Create 2 accounts
        val acc1Id = repository.addAccount(
            AccountEntity(name = "Bank BCA", type = AccountType.BANK, balance = 1000000.0)
        )
        val acc2Id = repository.addAccount(
            AccountEntity(name = "GoPay", type = AccountType.EWALLET, balance = 200000.0)
        )

        // Perform transfer
        val success = repository.transferBetweenAccounts(acc1Id, acc2Id, 150000.0, "Top up e-wallet")
        assertTrue(success)

        val acc1 = repository.getAccountById(acc1Id)
        val acc2 = repository.getAccountById(acc2Id)
        assertNotNull(acc1)
        assertNotNull(acc2)
        assertEquals(850000.0, acc1!!.balance, 0.01)
        assertEquals(350000.0, acc2!!.balance, 0.01)

        // Transactions created must have isTransfer = true
        val txs = repository.allTransactions.first()
        assertEquals(2, txs.size)
        assertTrue(txs.all { it.isTransfer })
    }

    @Test
    fun testRecurringAdvanceDateCalculation() = runBlocking {
        // Test direct calculateNextDueDate
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

        // Test executing recurring item advances date into the future
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

    @Test
    fun testFullDatabaseBackupAndRestore() = runBlocking {
        // Seed some data
        repository.addAccount(AccountEntity(name = "Main Vault", type = AccountType.CASH, balance = 500000.0))
        repository.addTransaction(
            TransactionEntity(
                title = "Grocery",
                amount = 75000.0,
                type = TransactionType.EXPENSE,
                category = TransactionCategory.FOOD,
                timestamp = System.currentTimeMillis()
            )
        )

        // Export full JSON backup
        val jsonBackup = repository.exportFullBackupJson()
        assertTrue(jsonBackup.contains("Main Vault"))
        assertTrue(jsonBackup.contains("Grocery"))

        // Clear and restore
        val restoreSuccess = repository.restoreFullBackupJson(jsonBackup)
        assertTrue(restoreSuccess)

        val accounts = repository.allAccounts.first()
        val txs = repository.allTransactions.first()
        assertEquals(1, accounts.size)
        assertEquals("Main Vault", accounts[0].name)
        assertEquals(1, txs.size)
        assertEquals("Grocery", txs[0].title)
    }
}
