package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class RecurringFrequency {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY
}

enum class RecurringCategoryType {
    GAJI,            // Salary
    UANG_BULANAN,    // Monthly allowance
    TAGIHAN,         // Utilities / Bills
    SUBSCRIPTION,    // Software / Streaming
    LAINNYA          // Other
}

@Entity(tableName = "recurring_transactions")
data class RecurringEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val category: TransactionCategory,
    val recurringCategory: RecurringCategoryType = RecurringCategoryType.SUBSCRIPTION,
    val frequency: RecurringFrequency = RecurringFrequency.MONTHLY,
    val nextDueDateMillis: Long,
    val autoExecute: Boolean = false,
    val isActive: Boolean = true,
    val lastExecutedMillis: Long? = null,
    val note: String = ""
)
