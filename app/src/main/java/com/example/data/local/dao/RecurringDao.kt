package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.RecurringEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringDao {
    @Query("SELECT * FROM recurring_transactions ORDER BY nextDueDateMillis ASC")
    fun getAllRecurring(): Flow<List<RecurringEntity>>

    @Query("SELECT * FROM recurring_transactions WHERE isActive = 1 ORDER BY nextDueDateMillis ASC")
    fun getActiveRecurring(): Flow<List<RecurringEntity>>

    @Query("SELECT * FROM recurring_transactions WHERE id = :id")
    suspend fun getRecurringById(id: Long): RecurringEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurring(item: RecurringEntity): Long

    @Update
    suspend fun updateRecurring(item: RecurringEntity)

    @Delete
    suspend fun deleteRecurring(item: RecurringEntity)

    @Query("DELETE FROM recurring_transactions WHERE id = :id")
    suspend fun deleteRecurringById(id: Long)

    @Query("UPDATE recurring_transactions SET lastExecutedMillis = :timestamp, nextDueDateMillis = :nextDue WHERE id = :id")
    suspend fun markExecuted(id: Long, timestamp: Long, nextDue: Long)
}
