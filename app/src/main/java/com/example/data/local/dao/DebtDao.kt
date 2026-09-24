package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.DebtEntity
import com.example.data.local.entity.DebtPaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DebtDao {
    @Query("SELECT * FROM debts ORDER BY dueDateMillis ASC")
    fun getAllDebts(): Flow<List<DebtEntity>>

    @Query("SELECT * FROM debts WHERE id = :id")
    suspend fun getDebtById(id: Long): DebtEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebt(debt: DebtEntity): Long

    @Update
    suspend fun updateDebt(debt: DebtEntity)

    @Delete
    suspend fun deleteDebt(debt: DebtEntity)

    @Query("DELETE FROM debts WHERE id = :id")
    suspend fun deleteDebtById(id: Long)

    @Query("UPDATE debts SET paidAmount = paidAmount + :paymentAmount, isSettled = CASE WHEN (paidAmount + :paymentAmount) >= amount THEN 1 ELSE 0 END WHERE id = :id")
    suspend fun recordPayment(id: Long, paymentAmount: Double)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: DebtPaymentEntity): Long

    @Query("SELECT * FROM debt_payments WHERE debtId = :debtId ORDER BY timestamp DESC")
    fun getPaymentsForDebt(debtId: Long): Flow<List<DebtPaymentEntity>>

    @Query("SELECT * FROM debt_payments ORDER BY timestamp DESC")
    fun getAllDebtPayments(): Flow<List<DebtPaymentEntity>>

    @Query("DELETE FROM debts")
    suspend fun deleteAllDebts()

    @Query("DELETE FROM debt_payments")
    suspend fun deleteAllDebtPayments()
}
