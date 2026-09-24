package com.example.data.util

import com.example.data.local.entity.TransactionCategory
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.TransactionType

data class CsvRowPreview(
    val lineNumber: Int,
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val category: TransactionCategory,
    val timestamp: Long,
    val note: String,
    val isValid: Boolean,
    val errorMessage: String? = null
)

data class CsvValidationSummary(
    val totalFound: Int,
    val validCount: Int,
    val invalidCount: Int,
    val validRows: List<CsvRowPreview>,
    val invalidRows: List<CsvRowPreview>
) {
    fun toTransactionEntities(targetAccountId: Long?): List<TransactionEntity> {
        return validRows.map { row ->
            TransactionEntity(
                title = row.title,
                amount = row.amount,
                type = row.type,
                category = row.category,
                timestamp = row.timestamp,
                note = row.note,
                isTransfer = false,
                accountId = targetAccountId
            )
        }
    }
}

object CsvValidator {

    fun validateCsv(csvText: String): CsvValidationSummary {
        val validRows = mutableListOf<CsvRowPreview>()
        val invalidRows = mutableListOf<CsvRowPreview>()
        val lines = csvText.lines()

        // Skip header if line contains header markers
        val startIndex = if (lines.isNotEmpty() && (lines[0].contains("title", ignoreCase = true) || lines[0].contains("amount", ignoreCase = true))) {
            1
        } else {
            0
        }

        var lineIndex = 1
        for (i in startIndex until lines.size) {
            val rawLine = lines[i].trim()
            if (rawLine.isBlank()) continue

            val lineNumber = i + 1
            // Handle comma split respecting quotes
            val parts = rawLine.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex())
                .map { it.replace("\"", "").trim() }

            if (parts.size < 4) {
                invalidRows.add(
                    CsvRowPreview(
                        lineNumber = lineNumber,
                        title = if (parts.isNotEmpty()) parts[0] else "Baris Kosong",
                        amount = 0.0,
                        type = TransactionType.EXPENSE,
                        category = TransactionCategory.OTHER,
                        timestamp = System.currentTimeMillis(),
                        note = rawLine,
                        isValid = false,
                        errorMessage = "Kolom kurang (minimal Judul, Jumlah, Tipe, Kategori)"
                    )
                )
                continue
            }

            val title = parts.getOrNull(if (startIndex == 1 && parts.size >= 5 && parts[0].toLongOrNull() != null) 1 else 0) ?: "Transaksi"
            val amountPartIndex = if (startIndex == 1 && parts.size >= 5 && parts[0].toLongOrNull() != null) 2 else 1
            val typePartIndex = amountPartIndex + 1
            val catPartIndex = typePartIndex + 1

            val rawAmount = parts.getOrNull(amountPartIndex)
            val parsedAmount = rawAmount?.toDoubleOrNull()

            if (parsedAmount == null || parsedAmount <= 0.0) {
                invalidRows.add(
                    CsvRowPreview(
                        lineNumber = lineNumber,
                        title = title,
                        amount = 0.0,
                        type = TransactionType.EXPENSE,
                        category = TransactionCategory.OTHER,
                        timestamp = System.currentTimeMillis(),
                        note = rawLine,
                        isValid = false,
                        errorMessage = "Nominal tidak valid ($rawAmount)"
                    )
                )
                continue
            }

            val rawType = parts.getOrNull(typePartIndex)?.uppercase() ?: ""
            val parsedType = try {
                TransactionType.valueOf(rawType)
            } catch (_: Exception) {
                if (rawType.contains("IN") || rawType.contains("MASUK")) TransactionType.INCOME else TransactionType.EXPENSE
            }

            val rawCat = parts.getOrNull(catPartIndex)?.uppercase() ?: ""
            val parsedCategory = try {
                TransactionCategory.valueOf(rawCat)
            } catch (_: Exception) {
                TransactionCategory.OTHER
            }

            val timestamp = parts.getOrNull(catPartIndex + 1)?.toLongOrNull() ?: System.currentTimeMillis()
            val note = parts.getOrNull(catPartIndex + 2) ?: ""

            validRows.add(
                CsvRowPreview(
                    lineNumber = lineNumber,
                    title = title,
                    amount = parsedAmount,
                    type = parsedType,
                    category = parsedCategory,
                    timestamp = timestamp,
                    note = note,
                    isValid = true,
                    errorMessage = null
                )
            )
            lineIndex++
        }

        return CsvValidationSummary(
            totalFound = validRows.size + invalidRows.size,
            validCount = validRows.size,
            invalidCount = invalidRows.size,
            validRows = validRows,
            invalidRows = invalidRows
        )
    }
}
