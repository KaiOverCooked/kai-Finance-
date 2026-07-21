package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class NotificationType {
    BILL_REMINDER, BUDGET_ALERT, SAVINGS_MILESTONE, SYSTEM
}

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val type: NotificationType = NotificationType.SYSTEM
)
