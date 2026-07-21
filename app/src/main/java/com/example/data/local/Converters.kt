package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.local.entity.NotificationType
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
}
