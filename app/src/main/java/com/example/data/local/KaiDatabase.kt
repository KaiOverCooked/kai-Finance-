package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.local.dao.BudgetDao
import com.example.data.local.dao.GoalDao
import com.example.data.local.dao.NotificationDao
import com.example.data.local.dao.TransactionDao
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.GoalEntity
import com.example.data.local.entity.NotificationEntity
import com.example.data.local.entity.TransactionEntity

@Database(
    entities = [
        TransactionEntity::class,
        BudgetEntity::class,
        GoalEntity::class,
        NotificationEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class KaiDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun goalDao(): GoalDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: KaiDatabase? = null

        fun getDatabase(context: Context): KaiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KaiDatabase::class.java,
                    "kai_finance_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
