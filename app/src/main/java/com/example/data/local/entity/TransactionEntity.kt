package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    EXPENSE, INCOME
}

enum class TransactionCategory {
    HOUSING, FOOD, TRANSPORT, ENTERTAINMENT, SHOPPING, UTILITIES, SALARY, INVESTMENT, HEALTH, OTHER
}

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val category: TransactionCategory,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = "",
    val imageUri: String? = null,
    val isRecurring: Boolean = false
)
