package com.habittracker.notification

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.habittracker.HabitTrackerApp
import com.habittracker.MainActivity
import com.habittracker.R
import com.habittracker.data.database.HabitDatabase
import com.habittracker.data.repository.HabitRepository
import java.time.LocalDate

class HabitReminderWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val habitId = inputData.getLong(KEY_HABIT_ID, -1)
        val habitName = inputData.getString(KEY_HABIT_NAME) ?: "Habit"

        if (habitId == -1L) return Result.failure()

        // Check if already completed today
        val database = HabitDatabase.getDatabase(context)
        val repository = HabitRepository(database.habitDao(), database.habitCompletionDao())

        val isCompleted = repository.isHabitCompletedForDate(habitId, LocalDate.now())
        if (isCompleted) {
            return Result.success()
        }

        showNotification(habitId, habitName)
        return Result.success()
    }

    private fun showNotification(habitId: Long, habitName: String) {
        // Check notification permission
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        // Intent to open app
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            habitId.toInt(),
            openIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Intent to mark as complete
        val completeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_MARK_COMPLETE
            putExtra(NotificationActionReceiver.EXTRA_HABIT_ID, habitId)
        }
        val completePendingIntent = PendingIntent.getBroadcast(
            context,
            habitId.toInt() + 1000,
            completeIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, HabitTrackerApp.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Time for: $habitName")
            .setContentText("Don't forget to complete your habit today!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openPendingIntent)
            .addAction(
                R.drawable.ic_check,
                "Mark Complete",
                completePendingIntent
            )
            .build()

        NotificationManagerCompat.from(context).notify(habitId.toInt(), notification)
    }

    companion object {
        const val KEY_HABIT_ID = "habit_id"
        const val KEY_HABIT_NAME = "habit_name"
    }
}
