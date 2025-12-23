package com.habittracker.notification

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.habittracker.data.model.Habit
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

class HabitNotificationScheduler(private val context: Context) {

    private val workManager = WorkManager.getInstance(context)

    fun scheduleHabitReminder(habit: Habit) {
        val reminderTimeStr = habit.reminderTime ?: return

        val timeParts = reminderTimeStr.split(":")
        val hour = timeParts[0].toIntOrNull() ?: return
        val minute = timeParts[1].toIntOrNull() ?: return

        val reminderTime = LocalTime.of(hour, minute)
        val now = LocalDateTime.now()
        var scheduledTime = now.toLocalDate().atTime(reminderTime)

        // If the time has already passed today, schedule for tomorrow
        if (scheduledTime.isBefore(now)) {
            scheduledTime = scheduledTime.plusDays(1)
        }

        val initialDelay = Duration.between(now, scheduledTime).toMinutes()

        val inputData = Data.Builder()
            .putLong(HabitReminderWorker.KEY_HABIT_ID, habit.id)
            .putString(HabitReminderWorker.KEY_HABIT_NAME, habit.name)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<HabitReminderWorker>(
            24, TimeUnit.HOURS
        )
            .setInitialDelay(initialDelay, TimeUnit.MINUTES)
            .setInputData(inputData)
            .addTag(getWorkTag(habit.id))
            .build()

        workManager.enqueueUniquePeriodicWork(
            getUniqueWorkName(habit.id),
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    fun cancelHabitReminder(habitId: Long) {
        workManager.cancelUniqueWork(getUniqueWorkName(habitId))
    }

    fun rescheduleAllReminders(habits: List<Habit>) {
        habits.filter { it.reminderTime != null }
            .forEach { scheduleHabitReminder(it) }
    }

    private fun getWorkTag(habitId: Long) = "habit_reminder_$habitId"
    private fun getUniqueWorkName(habitId: Long) = "habit_reminder_work_$habitId"
}
