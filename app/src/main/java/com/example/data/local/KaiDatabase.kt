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
    version = 4,
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

        val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Table creations for accounts, debts, debt_payments, recurring_transactions, investments if upgrading from v1
                db.execSQL("CREATE TABLE IF NOT EXISTS accounts (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `type` TEXT NOT NULL, `balance` REAL NOT NULL, `accountNumber` TEXT NOT NULL, `institutionName` TEXT NOT NULL, `isDefault` INTEGER NOT NULL, `colorHex` TEXT NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS debts (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `personName` TEXT NOT NULL, `amount` REAL NOT NULL, `paidAmount` REAL NOT NULL, `type` TEXT NOT NULL, `dueDateMillis` INTEGER NOT NULL, `isSettled` INTEGER NOT NULL, `notes` TEXT NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS debt_payments (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `debtId` INTEGER NOT NULL, `amount` REAL NOT NULL, `timestamp` INTEGER NOT NULL, `note` TEXT NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS recurring_transactions (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `amount` REAL NOT NULL, `type` TEXT NOT NULL, `category` TEXT NOT NULL, `recurringCategory` TEXT NOT NULL, `frequency` TEXT NOT NULL, `nextDueDateMillis` INTEGER NOT NULL, `autoExecute` INTEGER NOT NULL, `isActive` INTEGER NOT NULL, `lastExecutedMillis` INTEGER, `note` TEXT NOT NULL, `accountId` INTEGER)")
                db.execSQL("CREATE TABLE IF NOT EXISTS investments (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `assetName` TEXT NOT NULL, `symbol` TEXT NOT NULL, `quantity` REAL NOT NULL, `buyPrice` REAL NOT NULL, `currentPrice` REAL NOT NULL, `dividendReceived` REAL NOT NULL, `type` TEXT NOT NULL, `updatedAtMillis` INTEGER NOT NULL, `notes` TEXT NOT NULL)")
                try { db.execSQL("ALTER TABLE transactions ADD COLUMN accountId INTEGER DEFAULT NULL") } catch (e: Exception) {}
                try { db.execSQL("ALTER TABLE transactions ADD COLUMN transferToAccountId INTEGER DEFAULT NULL") } catch (e: Exception) {}
                try { db.execSQL("ALTER TABLE transactions ADD COLUMN isTransfer INTEGER NOT NULL DEFAULT 0") } catch (e: Exception) {}
                try { db.execSQL("ALTER TABLE transactions ADD COLUMN isRecurring INTEGER NOT NULL DEFAULT 0") } catch (e: Exception) {}
            }
        }

        val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE recurring_transactions ADD COLUMN accountId INTEGER DEFAULT NULL")
                } catch (e: Exception) {}
            }
        }

        val MIGRATION_3_4 = object : androidx.room.migration.Migration(3, 4) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                try {
                    db.execSQL("ALTER TABLE accounts ADD COLUMN initialBalance REAL NOT NULL DEFAULT 0.0")
                } catch (e: Exception) {}
            }
        }

        fun getDatabase(context: Context): KaiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KaiDatabase::class.java,
                    "kai_finance_db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
