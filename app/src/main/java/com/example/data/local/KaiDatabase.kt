package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.local.dao.AccountDao
import com.example.data.local.dao.BudgetDao
import com.example.data.local.dao.DebtDao
import com.example.data.local.dao.GoalDao
import com.example.data.local.dao.InvestmentDao
import com.example.data.local.dao.NotificationDao
import com.example.data.local.dao.RecurringDao
import com.example.data.local.dao.TransactionDao
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.DebtEntity
import com.example.data.local.entity.DebtPaymentEntity
import com.example.data.local.entity.GoalEntity
import com.example.data.local.entity.InvestmentEntity
import com.example.data.local.entity.NotificationEntity
import com.example.data.local.entity.RecurringEntity
import com.example.data.local.entity.TransactionEntity

@Database(
    entities = [
        TransactionEntity::class,
        BudgetEntity::class,
        GoalEntity::class,
        NotificationEntity::class,
        AccountEntity::class,
        DebtEntity::class,
        DebtPaymentEntity::class,
        RecurringEntity::class,
        InvestmentEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class KaiDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun goalDao(): GoalDao
    abstract fun notificationDao(): NotificationDao
    abstract fun accountDao(): AccountDao
    abstract fun debtDao(): DebtDao
    abstract fun recurringDao(): RecurringDao
    abstract fun investmentDao(): InvestmentDao

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
