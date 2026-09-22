package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class InvestmentType {
    STOCK,       // Saham (e.g. BBCA, BBRI, AAPL)
    CRYPTO,      // Kripto (e.g. BTC, ETH)
    MUTUAL_FUND, // Reksadana
    BOND,        // Obligasi / SBN
    GOLD,        // Emas
    OTHER
}

@Entity(tableName = "investments")
data class InvestmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val assetName: String,
    val symbol: String,
    val quantity: Double,
    val buyPrice: Double,
    val currentPrice: Double,
    val dividendReceived: Double = 0.0,
    val type: InvestmentType = InvestmentType.STOCK,
    val notes: String = "",
    val updatedAtMillis: Long = System.currentTimeMillis()
) {
    val totalCost: Double
        get() = quantity * buyPrice

    val currentValue: Double
        get() = quantity * currentPrice

    val unrealizedGainLoss: Double
        get() = currentValue - totalCost

    val gainLossPercentage: Double
        get() = if (totalCost > 0) (unrealizedGainLoss / totalCost) * 100 else 0.0

    val totalReturn: Double
        get() = unrealizedGainLoss + dividendReceived
}
