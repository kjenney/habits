package com.habittracker.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.habittracker.data.database.HabitDatabase
import com.habittracker.data.repository.HabitRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class NotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_MARK_COMPLETE -> {
                val habitId = intent.getLongExtra(EXTRA_HABIT_ID, -1)
                if (habitId != -1L) {
                    markHabitComplete(context, habitId)
                }
            }
        }
    }

    private fun markHabitComplete(context: Context, habitId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            val database = HabitDatabase.getDatabase(context)
            val repository = HabitRepository(database.habitDao(), database.habitCompletionDao())
            repository.markHabitComplete(habitId, LocalDate.now())

            // Dismiss the notification
            NotificationManagerCompat.from(context).cancel(habitId.toInt())
        }
    }

    companion object {
        const val ACTION_MARK_COMPLETE = "com.habittracker.ACTION_MARK_COMPLETE"
        const val EXTRA_HABIT_ID = "extra_habit_id"
    }
}
