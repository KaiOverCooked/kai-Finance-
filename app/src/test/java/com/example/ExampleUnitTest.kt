package com.example

import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.AccountType
import com.example.data.local.entity.DebtEntity
import com.example.data.local.entity.DebtType
import com.example.data.local.entity.InvestmentEntity
import com.example.data.local.entity.InvestmentType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

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
}
