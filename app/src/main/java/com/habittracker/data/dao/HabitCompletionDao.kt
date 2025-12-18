package com.habittracker.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.habittracker.data.model.HabitCompletion
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitCompletionDao {
    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId ORDER BY date DESC")
    fun getCompletionsForHabit(habitId: Long): Flow<List<HabitCompletion>>

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId AND date = :date LIMIT 1")
    suspend fun getCompletionForDate(habitId: Long, date: String): HabitCompletion?

    @Query("SELECT * FROM habit_completions WHERE date = :date")
    fun getCompletionsForDate(date: String): Flow<List<HabitCompletion>>

    @Query("SELECT * FROM habit_completions WHERE date BETWEEN :startDate AND :endDate ORDER BY date")
    fun getCompletionsInRange(startDate: String, endDate: String): Flow<List<HabitCompletion>>

    @Query("SELECT * FROM habit_completions ORDER BY date DESC")
    suspend fun getAllCompletions(): List<HabitCompletion>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletion(completion: HabitCompletion): Long

    @Delete
    suspend fun deleteCompletion(completion: HabitCompletion)

    @Query("DELETE FROM habit_completions WHERE habitId = :habitId AND date = :date")
    suspend fun deleteCompletionForDate(habitId: Long, date: String)

    @Query("SELECT COUNT(*) FROM habit_completions WHERE habitId = :habitId")
    suspend fun getCompletionCountForHabit(habitId: Long): Int

    @Query("SELECT COUNT(*) FROM habit_completions WHERE habitId = :habitId AND date BETWEEN :startDate AND :endDate")
    suspend fun getCompletionCountInRange(habitId: Long, startDate: String, endDate: String): Int
}
