package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.KaiDatabase
import com.example.data.local.entity.GoalEntity
import com.example.data.repository.FinanceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GoalsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FinanceRepository(KaiDatabase.getDatabase(application))

    val goals: StateFlow<List<GoalEntity>> = repository.allGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createGoal(name: String, targetAmount: Double, currentAmount: Double, targetDateMillis: Long, category: String) {
        viewModelScope.launch {
            repository.addGoal(
                GoalEntity(
                    name = name,
                    targetAmount = targetAmount,
                    currentAmount = currentAmount,
                    targetDateMillis = targetDateMillis,
                    category = category,
                    isAchieved = currentAmount >= targetAmount
                )
            )
        }
    }

    fun contributeToGoal(goal: GoalEntity, amount: Double) {
        viewModelScope.launch {
            val newAmount = goal.currentAmount + amount
            val updatedGoal = goal.copy(
                currentAmount = newAmount,
                isAchieved = newAmount >= goal.targetAmount
            )
            repository.updateGoal(updatedGoal)
        }
    }

    fun deleteGoal(id: Long) {
        viewModelScope.launch {
            repository.deleteGoalById(id)
        }
    }
}
