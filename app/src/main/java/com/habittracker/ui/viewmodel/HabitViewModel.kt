package com.habittracker.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.habittracker.data.database.HabitDatabase
import com.habittracker.data.model.Habit
import com.habittracker.data.model.HabitWithCompletions
import com.habittracker.data.repository.HabitRepository
import com.habittracker.notification.HabitNotificationScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class HabitViewModel(application: Application) : AndroidViewModel(application) {
    private val database = HabitDatabase.getDatabase(application)
    private val repository = HabitRepository(
        database.habitDao(),
        database.habitCompletionDao()
    )
    private val notificationScheduler = HabitNotificationScheduler(application)

    val habitsWithCompletions: StateFlow<List<HabitWithCompletions>> =
        repository.getAllHabitsWithCompletions()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun addHabit(name: String, description: String, reminderTime: String?, reminderEnabled: Boolean = true) {
        viewModelScope.launch {
            val habit = Habit(
                name = name,
                description = description,
                reminderTime = reminderTime,
                reminderEnabled = true
            )
            val habitId = repository.insertHabit(habit)
            notificationScheduler.scheduleHabitReminder(habit.copy(id = habitId))
        }
    }

    fun updateHabit(habit: Habit) {
        viewModelScope.launch {
            repository.updateHabit(habit)
            notificationScheduler.scheduleHabitReminder(habit)
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            notificationScheduler.cancelHabitReminder(habit.id)
            repository.deleteHabit(habit)
        }
    }

    fun archiveHabit(habitId: Long) {
        viewModelScope.launch {
            notificationScheduler.cancelHabitReminder(habitId)
            repository.archiveHabit(habitId)
        }
    }

    fun toggleHabitCompletion(habitId: Long, date: LocalDate = _selectedDate.value) {
        viewModelScope.launch {
            repository.toggleHabitCompletion(habitId, date)
        }
    }

    fun isHabitCompletedForDate(habitId: Long, date: LocalDate): Boolean {
        val habitWithCompletions = habitsWithCompletions.value.find { it.habit.id == habitId }
        val dateString = date.toString()
        return habitWithCompletions?.completions?.any { it.date == dateString } == true
    }

    suspend fun getHabitById(habitId: Long): Habit? {
        return repository.getHabitById(habitId)
    }
}
