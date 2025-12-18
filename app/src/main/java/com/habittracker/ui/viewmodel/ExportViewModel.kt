package com.habittracker.ui.viewmodel

import android.app.Application
import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.habittracker.data.database.HabitDatabase
import com.habittracker.data.model.Habit
import com.habittracker.data.model.HabitCompletion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

sealed class ExportState {
    object Idle : ExportState()
    object Exporting : ExportState()
    data class Success(val filePath: String) : ExportState()
    data class Error(val message: String) : ExportState()
}

class ExportViewModel(application: Application) : AndroidViewModel(application) {
    private val database = HabitDatabase.getDatabase(application)

    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState: StateFlow<ExportState> = _exportState.asStateFlow()

    fun exportToCsv() {
        viewModelScope.launch {
            _exportState.value = ExportState.Exporting
            try {
                val habits = database.habitDao().getAllHabits().first()
                val completions = database.habitCompletionDao().getAllCompletions()

                val csvContent = generateCsvContent(habits, completions)
                val filePath = saveCsvFile(csvContent)

                _exportState.value = ExportState.Success(filePath)
            } catch (e: Exception) {
                _exportState.value = ExportState.Error(e.message ?: "Export failed")
            }
        }
    }

    private fun generateCsvContent(habits: List<Habit>, completions: List<HabitCompletion>): String {
        val sb = StringBuilder()

        // Header
        sb.appendLine("Habit ID,Habit Name,Description,Reminder Time,Reminder Enabled,Completion Date,Completed At")

        // Create a map of habit id to habit for quick lookup
        val habitMap = habits.associateBy { it.id }

        // If there are completions, export them with habit details
        if (completions.isNotEmpty()) {
            for (completion in completions) {
                val habit = habitMap[completion.habitId]
                if (habit != null) {
                    sb.appendLine(
                        "${habit.id}," +
                        "\"${habit.name.replace("\"", "\"\"")}\"," +
                        "\"${habit.description.replace("\"", "\"\"")}\"," +
                        "${habit.reminderTime ?: ""}," +
                        "${habit.reminderEnabled}," +
                        "${completion.date}," +
                        "${completion.completedAt}"
                    )
                }
            }
        }

        // Also include habits without completions
        val habitsWithCompletions = completions.map { it.habitId }.toSet()
        for (habit in habits) {
            if (habit.id !in habitsWithCompletions) {
                sb.appendLine(
                    "${habit.id}," +
                    "\"${habit.name.replace("\"", "\"\"")}\"," +
                    "\"${habit.description.replace("\"", "\"\"")}\"," +
                    "${habit.reminderTime ?: ""}," +
                    "${habit.reminderEnabled}," +
                    "," // No completion date
                )
            }
        }

        return sb.toString()
    }

    private suspend fun saveCsvFile(content: String): String = withContext(Dispatchers.IO) {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        val fileName = "habit_tracker_export_$timestamp.csv"

        val context = getApplication<Application>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Use MediaStore for Android 10+
            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "text/csv")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val uri = context.contentResolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                contentValues
            ) ?: throw Exception("Failed to create file")

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(content.toByteArray())
            } ?: throw Exception("Failed to open output stream")

            "Downloads/$fileName"
        } else {
            // Legacy storage for older Android versions
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)
            FileOutputStream(file).use { outputStream ->
                outputStream.write(content.toByteArray())
            }
            file.absolutePath
        }
    }

    fun resetExportState() {
        _exportState.value = ExportState.Idle
    }
}
