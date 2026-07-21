package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: TransactionCategory,
    val limitAmount: Double,
    val spentAmount: Double = 0.0,
    val period: String = "Monthly" // Monthly, Weekly, Yearly
)
