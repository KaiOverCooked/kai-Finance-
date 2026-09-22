package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class DebtType {
    HUTANG,  // We owe money to someone (Payable)
    PIUTANG  // Someone owes money to us (Receivable)
}

@Entity(tableName = "debts")
data class DebtEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val personName: String,
    val amount: Double,
    val paidAmount: Double = 0.0,
    val type: DebtType,
    val dueDateMillis: Long,
    val isSettled: Boolean = false,
    val notes: String = "",
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "debt_payments")
data class DebtPaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val debtId: Long,
    val amount: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = ""
)
