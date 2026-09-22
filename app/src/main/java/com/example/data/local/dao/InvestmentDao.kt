package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.InvestmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InvestmentDao {
    @Query("SELECT * FROM investments ORDER BY updatedAtMillis DESC")
    fun getAllInvestments(): Flow<List<InvestmentEntity>>

    @Query("SELECT * FROM investments WHERE id = :id")
    suspend fun getInvestmentById(id: Long): InvestmentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvestment(item: InvestmentEntity): Long

    @Update
    suspend fun updateInvestment(item: InvestmentEntity)

    @Delete
    suspend fun deleteInvestment(item: InvestmentEntity)

    @Query("DELETE FROM investments WHERE id = :id")
    suspend fun deleteInvestmentById(id: Long)

    @Query("UPDATE investments SET currentPrice = :newPrice, updatedAtMillis = :now WHERE id = :id")
    suspend fun updateCurrentPrice(id: Long, newPrice: Double, now: Long = System.currentTimeMillis())

    @Query("UPDATE investments SET dividendReceived = dividendReceived + :dividend, updatedAtMillis = :now WHERE id = :id")
    suspend fun addDividend(id: Long, dividend: Double, now: Long = System.currentTimeMillis())
}
