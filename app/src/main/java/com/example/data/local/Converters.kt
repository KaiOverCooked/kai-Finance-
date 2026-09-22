package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.local.entity.AccountType
import com.example.data.local.entity.DebtType
import com.example.data.local.entity.InvestmentType
import com.example.data.local.entity.NotificationType
import com.example.data.local.entity.RecurringCategoryType
import com.example.data.local.entity.RecurringFrequency
import com.example.data.local.entity.TransactionCategory
import com.example.data.local.entity.TransactionType

class Converters {
    @TypeConverter
    fun fromTransactionType(type: TransactionType): String = type.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType = try {
        TransactionType.valueOf(value)
    } catch (e: Exception) {
        TransactionType.EXPENSE
    }

    @TypeConverter
    fun fromTransactionCategory(category: TransactionCategory): String = category.name

    @TypeConverter
    fun toTransactionCategory(value: String): TransactionCategory = try {
        TransactionCategory.valueOf(value)
    } catch (e: Exception) {
        TransactionCategory.OTHER
    }

    @TypeConverter
    fun fromNotificationType(type: NotificationType): String = type.name

    @TypeConverter
    fun toNotificationType(value: String): NotificationType = try {
        NotificationType.valueOf(value)
    } catch (e: Exception) {
        NotificationType.SYSTEM
    }

    @TypeConverter
    fun fromAccountType(type: AccountType): String = type.name

    @TypeConverter
    fun toAccountType(value: String): AccountType = try {
        AccountType.valueOf(value)
    } catch (e: Exception) {
        AccountType.BANK
    }

    @TypeConverter
    fun fromDebtType(type: DebtType): String = type.name

    @TypeConverter
    fun toDebtType(value: String): DebtType = try {
        DebtType.valueOf(value)
    } catch (e: Exception) {
        DebtType.HUTANG
    }

    @TypeConverter
    fun fromRecurringFrequency(frequency: RecurringFrequency): String = frequency.name

    @TypeConverter
    fun toRecurringFrequency(value: String): RecurringFrequency = try {
        RecurringFrequency.valueOf(value)
    } catch (e: Exception) {
        RecurringFrequency.MONTHLY
    }

    @TypeConverter
    fun fromRecurringCategoryType(category: RecurringCategoryType): String = category.name

    @TypeConverter
    fun toRecurringCategoryType(value: String): RecurringCategoryType = try {
        RecurringCategoryType.valueOf(value)
    } catch (e: Exception) {
        RecurringCategoryType.SUBSCRIPTION
    }

    @TypeConverter
    fun fromInvestmentType(type: InvestmentType): String = type.name

    @TypeConverter
    fun toInvestmentType(value: String): InvestmentType = try {
        InvestmentType.valueOf(value)
    } catch (e: Exception) {
        InvestmentType.STOCK
    }
}
