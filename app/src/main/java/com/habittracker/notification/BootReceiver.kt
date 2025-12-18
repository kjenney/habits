package com.habittracker.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.habittracker.data.database.HabitDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            rescheduleAllReminders(context)
        }
    }

    private fun rescheduleAllReminders(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val database = HabitDatabase.getDatabase(context)
            val habitsWithReminders = database.habitDao().getHabitsWithReminders()

            val scheduler = HabitNotificationScheduler(context)
            scheduler.rescheduleAllReminders(habitsWithReminders)
        }
    }
}
