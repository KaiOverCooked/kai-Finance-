package com.example

import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.AccountType
import com.example.data.local.entity.DebtEntity
import com.example.data.local.entity.DebtType
import com.example.data.local.entity.InvestmentEntity
import com.example.data.local.entity.InvestmentType
import com.example.data.local.entity.TransactionCategory
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.TransactionType
import com.example.ui.util.CurrencyFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ExampleUnitTest {
    @Test
    fun testAccountBalanceCalculation() {
        val acc1 = AccountEntity(id = 1, name = "BCA", type = AccountType.BANK, balance = 5000000.0)
        val acc2 = AccountEntity(id = 2, name = "GoPay", type = AccountType.EWALLET, balance = 250000.0)
        val total = listOf(acc1, acc2).sumOf { it.balance }
        assertEquals(5250000.0, total, 0.001)
    }

    @Test
    fun testDebtRemainingCalculation() {
        val debt = DebtEntity(
            id = 1,
            personName = "Budi",
            amount = 1000000.0,
            paidAmount = 400000.0,
            type = DebtType.PIUTANG,
            dueDateMillis = System.currentTimeMillis() + 86400000L
        )
        val remaining = debt.amount - debt.paidAmount
        assertEquals(600000.0, remaining, 0.001)
        assertEquals(false, debt.isSettled)
    }

    @Test
    fun testInvestmentProfitCalculation() {
        val inv = InvestmentEntity(
            id = 1,
            assetName = "Bank Central Asia",
            symbol = "BBCA",
            quantity = 100.0,
            buyPrice = 9000.0,
            currentPrice = 10000.0,
            type = InvestmentType.STOCK,
            dividendReceived = 50000.0
        )
        assertEquals(900000.0, inv.totalCost, 0.001)
        assertEquals(1000000.0, inv.currentValue, 0.001)
        assertEquals(100000.0, inv.unrealizedGainLoss, 0.001)
        assertTrue(inv.gainLossPercentage > 11.0)
        assertEquals(150000.0, inv.totalReturn, 0.001)
    }

    // TEST ANALYTICS: Pastikan Daily, Weekly, Monthly, Yearly mengambil data asli tanpa transfer
    @Test
    fun testAnalyticsDataFiltering() {
        val now = System.currentTimeMillis()
        val dayMs = 24 * 60 * 60 * 1000L

        val transactions = listOf(
            TransactionEntity(id = 1, title = "Makan Siang", amount = 50000.0, type = TransactionType.EXPENSE, category = TransactionCategory.FOOD, timestamp = now),
            TransactionEntity(id = 2, title = "Bensin", amount = 100000.0, type = TransactionType.EXPENSE, category = TransactionCategory.TRANSPORT, timestamp = now - 2 * dayMs),
            TransactionEntity(id = 3, title = "Gaji", amount = 8000000.0, type = TransactionType.INCOME, category = TransactionCategory.SALARY, timestamp = now - 5 * dayMs),
            TransactionEntity(id = 4, title = "Transfer BCA ke GoPay", amount = 500000.0, type = TransactionType.EXPENSE, category = TransactionCategory.OTHER, timestamp = now, isTransfer = true)
        )

        // Filter out transfers
        val realTransactions = transactions.filter { !it.isTransfer }
        assertEquals(3, realTransactions.size)

        val totalIncome = realTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val totalExpense = realTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

        assertEquals(8000000.0, totalIncome, 0.001)
        assertEquals(150000.0, totalExpense, 0.001)
    }

    // TEST FORMAT RUPIAH
    @Test
    fun testIndonesianRupiahFormatting() {
        val formatted1 = CurrencyFormatter.format(1500000.0, "Rp")
        assertEquals("Rp 1.500.000", formatted1)

        val formatted2 = CurrencyFormatter.format(25000.0, "Rp")
        assertEquals("Rp 25.000", formatted2)

        val plain = CurrencyFormatter.formatPlain(10000000.0)
        assertEquals("10.000.000", plain)
    }
}
