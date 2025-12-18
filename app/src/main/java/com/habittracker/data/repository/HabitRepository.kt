package com.habittracker.data.repository

import com.habittracker.data.dao.HabitCompletionDao
import com.habittracker.data.dao.HabitDao
import com.habittracker.data.model.Habit
import com.habittracker.data.model.HabitCompletion
import com.habittracker.data.model.HabitWithCompletions
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HabitRepository(
    private val habitDao: HabitDao,
    private val completionDao: HabitCompletionDao
) {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    // Habit operations
    fun getAllActiveHabits(): Flow<List<Habit>> = habitDao.getAllActiveHabits()

    fun getAllHabitsWithCompletions(): Flow<List<HabitWithCompletions>> =
        habitDao.getAllHabitsWithCompletions()

    fun getHabitWithCompletions(habitId: Long): Flow<HabitWithCompletions?> =
        habitDao.getHabitWithCompletions(habitId)

    suspend fun getHabitById(habitId: Long): Habit? = habitDao.getHabitById(habitId)

    suspend fun getHabitsWithReminders(): List<Habit> = habitDao.getHabitsWithReminders()

    suspend fun insertHabit(habit: Habit): Long = habitDao.insertHabit(habit)

    suspend fun updateHabit(habit: Habit) = habitDao.updateHabit(habit)

    suspend fun deleteHabit(habit: Habit) = habitDao.deleteHabit(habit)

    suspend fun archiveHabit(habitId: Long) = habitDao.setHabitArchived(habitId, true)

    // Completion operations
    fun getCompletionsForDate(date: LocalDate): Flow<List<HabitCompletion>> =
        completionDao.getCompletionsForDate(date.format(dateFormatter))

    suspend fun isHabitCompletedForDate(habitId: Long, date: LocalDate): Boolean {
        val dateString = date.format(dateFormatter)
        return completionDao.getCompletionForDate(habitId, dateString) != null
    }

    suspend fun toggleHabitCompletion(habitId: Long, date: LocalDate) {
        val dateString = date.format(dateFormatter)
        val existing = completionDao.getCompletionForDate(habitId, dateString)
        if (existing != null) {
            completionDao.deleteCompletion(existing)
        } else {
            completionDao.insertCompletion(
                HabitCompletion(habitId = habitId, date = dateString)
            )
        }
    }

    suspend fun markHabitComplete(habitId: Long, date: LocalDate) {
        val dateString = date.format(dateFormatter)
        val existing = completionDao.getCompletionForDate(habitId, dateString)
        if (existing == null) {
            completionDao.insertCompletion(
                HabitCompletion(habitId = habitId, date = dateString)
            )
        }
    }

    suspend fun markHabitIncomplete(habitId: Long, date: LocalDate) {
        val dateString = date.format(dateFormatter)
        completionDao.deleteCompletionForDate(habitId, dateString)
    }

    fun getCompletionsInRange(startDate: LocalDate, endDate: LocalDate): Flow<List<HabitCompletion>> =
        completionDao.getCompletionsInRange(
            startDate.format(dateFormatter),
            endDate.format(dateFormatter)
        )

    suspend fun getCompletionCountForHabit(habitId: Long): Int =
        completionDao.getCompletionCountForHabit(habitId)

    // For CSV export
    suspend fun getAllHabitsForExport(): List<Habit> {
        var habits: List<Habit> = emptyList()
        habitDao.getAllHabits().collect { habits = it }
        return habits
    }

    suspend fun getAllCompletions(): List<HabitCompletion> =
        completionDao.getAllCompletions()
}
