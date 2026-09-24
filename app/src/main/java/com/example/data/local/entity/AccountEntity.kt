package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AccountType {
    CASH,
    BANK,
    EWALLET
}

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: AccountType,
    val balance: Double,
    val initialBalance: Double = 0.0,
    val accountNumber: String = "",
    val institutionName: String = "",
    val colorHex: String = "#FFFFFF",
    val isDefault: Boolean = false
)
