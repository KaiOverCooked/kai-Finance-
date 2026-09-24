package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.KaiDatabase
import com.example.data.local.entity.TransactionCategory
import com.example.data.local.entity.TransactionEntity
import com.example.data.local.entity.TransactionType
import com.example.data.repository.FinanceRepository
import com.example.ui.components.BarGroup
import com.example.ui.components.PieSegment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class TimePeriod { DAILY, WEEKLY, MONTHLY, YEARLY }

data class AnalyticsUiState(
    val timePeriod: TimePeriod = TimePeriod.MONTHLY,
    val totalExpense: Double = 0.0,
    val totalIncome: Double = 0.0,
    val pieSegments: List<PieSegment> = emptyList(),
    val barGroups: List<BarGroup> = emptyList(),
    val categoryBreakdown: List<Pair<TransactionCategory, Double>> = emptyList()
)

class AnalyticsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FinanceRepository(KaiDatabase.getDatabase(application))
    val selectedPeriod = MutableStateFlow(TimePeriod.MONTHLY)

    private val modernPalette = listOf(
        Color(0xFFFFFFFF),
        Color(0xFFB0BEC5),
        Color(0xFF78909C),
        Color(0xFF455A64),
        Color(0xFF4CAF50),
        Color(0xFF29B6F6),
        Color(0xFFFFA726),
        Color(0xFFAB47BC),
        Color(0xFFFF7043),
        Color(0xFF26A69A),
        Color(0xFFEC407A),
        Color(0xFF8D6E63)
    )

    val uiState: StateFlow<AnalyticsUiState> = combine(
        repository.allTransactions,
        selectedPeriod
    ) { list, period ->
        // Exclude transfers: transfers must never be counted as income/expense
        val nonTransferList = list.filter { !it.isTransfer }
        val filteredList = filterTransactionsByPeriod(nonTransferList, period)

        val expenseList = filteredList.filter { it.type == TransactionType.EXPENSE }
        val incomeList = filteredList.filter { it.type == TransactionType.INCOME }

        val totalExp = expenseList.sumOf { it.amount }
        val totalInc = incomeList.sumOf { it.amount }

        val categorySums = expenseList.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        val pieSegments = categorySums.entries.mapIndexed { index, entry ->
            PieSegment(
                categoryName = entry.key.name.lowercase().replaceFirstChar { it.uppercase() },
                value = entry.value,
                color = modernPalette[index % modernPalette.size]
            )
        }

        // 100% Real Transaction Distribution Bar Groups
        val barGroups = generateRealBarGroups(nonTransferList, period)

        AnalyticsUiState(
            timePeriod = period,
            totalExpense = totalExp,
            totalIncome = totalInc,
            pieSegments = pieSegments,
            barGroups = barGroups,
            categoryBreakdown = categorySums.toList().sortedByDescending { it.second }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AnalyticsUiState())

    private fun filterTransactionsByPeriod(list: List<TransactionEntity>, period: TimePeriod): List<TransactionEntity> {
        val (startMs, endMs) = getTimePeriodBounds(period)
        return list.filter { it.timestamp in startMs..endMs }
    }

    private fun getTimePeriodBounds(period: TimePeriod): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        val endMs = cal.timeInMillis

        when (period) {
            TimePeriod.DAILY -> {
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                return Pair(cal.timeInMillis, endMs)
            }
            TimePeriod.WEEKLY -> {
                cal.firstDayOfWeek = Calendar.MONDAY
                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                return Pair(cal.timeInMillis, endMs)
            }
            TimePeriod.MONTHLY -> {
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                return Pair(cal.timeInMillis, endMs)
            }
            TimePeriod.YEARLY -> {
                cal.set(Calendar.DAY_OF_YEAR, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                return Pair(cal.timeInMillis, endMs)
            }
        }
    }

    private fun generateRealBarGroups(list: List<TransactionEntity>, period: TimePeriod): List<BarGroup> {
        val cal = Calendar.getInstance()

        return when (period) {
            TimePeriod.DAILY -> {
                // Today: 4 time intervals (00-06, 06-12, 12-18, 18-24)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                val startOfDay = cal.timeInMillis

                val sixHoursMs = 6L * 60 * 60 * 1000
                listOf(
                    Pair("00-06", startOfDay to startOfDay + sixHoursMs),
                    Pair("06-12", startOfDay + sixHoursMs to startOfDay + 2 * sixHoursMs),
                    Pair("12-18", startOfDay + 2 * sixHoursMs to startOfDay + 3 * sixHoursMs),
                    Pair("18-24", startOfDay + 3 * sixHoursMs to startOfDay + 4 * sixHoursMs)
                ).map { (label, range) ->
                    val inc = list.filter { it.type == TransactionType.INCOME && it.timestamp in range.first until range.second }.sumOf { it.amount }
                    val exp = list.filter { it.type == TransactionType.EXPENSE && it.timestamp in range.first until range.second }.sumOf { it.amount }
                    BarGroup(label, inc, exp)
                }
            }

            TimePeriod.WEEKLY -> {
                // 7 days of the current week (Mon to Sun)
                cal.firstDayOfWeek = Calendar.MONDAY
                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)

                val dayNames = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")
                val dayMs = 24L * 60 * 60 * 1000

                (0..6).map { dayIndex ->
                    val dayStart = cal.timeInMillis + (dayIndex * dayMs)
                    val dayEnd = dayStart + dayMs
                    val inc = list.filter { it.type == TransactionType.INCOME && it.timestamp in dayStart until dayEnd }.sumOf { it.amount }
                    val exp = list.filter { it.type == TransactionType.EXPENSE && it.timestamp in dayStart until dayEnd }.sumOf { it.amount }
                    BarGroup(dayNames[dayIndex], inc, exp)
                }
            }

            TimePeriod.MONTHLY -> {
                // Current month: 4 real weekly segments (W1: 1-7, W2: 8-14, W3: 15-21, W4: 22-end)
                val currentYear = cal.get(Calendar.YEAR)
                val currentMonth = cal.get(Calendar.MONTH)
                val maxDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

                fun getRange(startDay: Int, endDay: Int): Pair<Long, Long> {
                    val c = Calendar.getInstance()
                    c.set(currentYear, currentMonth, startDay, 0, 0, 0)
                    c.set(Calendar.MILLISECOND, 0)
                    val s = c.timeInMillis
                    c.set(currentYear, currentMonth, endDay, 23, 59, 59)
                    c.set(Calendar.MILLISECOND, 999)
                    val e = c.timeInMillis
                    return Pair(s, e)
                }

                val weeks = listOf(
                    "W1" to getRange(1, 7),
                    "W2" to getRange(8, 14),
                    "W3" to getRange(15, 21),
                    "W4" to getRange(22, maxDay)
                )

                weeks.map { (label, range) ->
                    val inc = list.filter { it.type == TransactionType.INCOME && it.timestamp in range.first..range.second }.sumOf { it.amount }
                    val exp = list.filter { it.type == TransactionType.EXPENSE && it.timestamp in range.first..range.second }.sumOf { it.amount }
                    BarGroup(label, inc, exp)
                }
            }

            TimePeriod.YEARLY -> {
                // Current year: 4 quarters (Q1: Jan-Mar, Q2: Apr-Jun, Q3: Jul-Sep, Q4: Oct-Dec)
                val currentYear = cal.get(Calendar.YEAR)

                fun getQuarterRange(startMonth: Int, endMonth: Int): Pair<Long, Long> {
                    val c = Calendar.getInstance()
                    c.set(currentYear, startMonth, 1, 0, 0, 0)
                    c.set(Calendar.MILLISECOND, 0)
                    val s = c.timeInMillis
                    c.set(currentYear, endMonth, 1)
                    val lastDay = c.getActualMaximum(Calendar.DAY_OF_MONTH)
                    c.set(currentYear, endMonth, lastDay, 23, 59, 59)
                    c.set(Calendar.MILLISECOND, 999)
                    val e = c.timeInMillis
                    return Pair(s, e)
                }

                val quarters = listOf(
                    "Q1" to getQuarterRange(Calendar.JANUARY, Calendar.MARCH),
                    "Q2" to getQuarterRange(Calendar.APRIL, Calendar.JUNE),
                    "Q3" to getQuarterRange(Calendar.JULY, Calendar.SEPTEMBER),
                    "Q4" to getQuarterRange(Calendar.OCTOBER, Calendar.DECEMBER)
                )

                quarters.map { (label, range) ->
                    val inc = list.filter { it.type == TransactionType.INCOME && it.timestamp in range.first..range.second }.sumOf { it.amount }
                    val exp = list.filter { it.type == TransactionType.EXPENSE && it.timestamp in range.first..range.second }.sumOf { it.amount }
                    BarGroup(label, inc, exp)
                }
            }
        }
    }
}
