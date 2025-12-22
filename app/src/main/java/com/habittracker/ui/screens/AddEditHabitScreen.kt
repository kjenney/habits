package com.habittracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.habittracker.data.model.Habit
import com.habittracker.ui.viewmodel.HabitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditHabitScreen(
    viewModel: HabitViewModel,
    habitId: Long?,
    onNavigateBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var reminderEnabled by remember { mutableStateOf(false) }
    var reminderTime by remember { mutableStateOf<String?>(null) }
    var existingHabit by remember { mutableStateOf<Habit?>(null) }
    var showTimePicker by remember { mutableStateOf(false) }

    val isEditing = habitId != null

    // Load existing habit if editing
    LaunchedEffect(habitId) {
        if (habitId != null) {
            viewModel.getHabitById(habitId)?.let { habit ->
                existingHabit = habit
                name = habit.name
                description = habit.description
                reminderEnabled = habit.reminderEnabled
                reminderTime = habit.reminderTime
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Edit Habit" else "Add Habit") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Habit Name") },
                placeholder = { Text("e.g., Exercise, Read, Meditate") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (optional)") },
                placeholder = { Text("Add details about your habit") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Reminder section
            Text(
                text = "Reminder",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Enable daily reminder",
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = reminderEnabled,
                    onCheckedChange = { reminderEnabled = it }
                )
            }

            if (reminderEnabled) {
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { showTimePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null
                    )
                    Text(
                        text = reminderTime?.let { formatTimeForDisplay(it) } ?: "Set reminder time",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        if (isEditing && existingHabit != null) {
                            viewModel.updateHabit(
                                existingHabit!!.copy(
                                    name = name.trim(),
                                    description = description.trim(),
                                    reminderEnabled = reminderEnabled,
                                    reminderTime = if (reminderEnabled) reminderTime else null
                                )
                            )
                        } else {
                            viewModel.addHabit(
                                name = name.trim(),
                                description = description.trim(),
                                reminderTime = if (reminderEnabled) reminderTime else null,
                                reminderEnabled = reminderEnabled
                            )
                        }
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank()
            ) {
                Text(if (isEditing) "Save Changes" else "Add Habit")
            }
        }
    }

    // Time picker dialog
    if (showTimePicker) {
        TimePickerDialog(
            initialTime = reminderTime,
            onConfirm = { time ->
                reminderTime = time
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }
}

@Composable
fun TimePickerDialog(
    initialTime: String?,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val initialHour = initialTime?.split(":")?.getOrNull(0)?.toIntOrNull() ?: 9
    val initialMinute = initialTime?.split(":")?.getOrNull(1)?.toIntOrNull() ?: 0

    var selectedHour by remember { mutableIntStateOf(initialHour) }
    var selectedMinute by remember { mutableIntStateOf(initialMinute) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Select Time",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Time display
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Hour selector
                    NumberPicker(
                        value = selectedHour,
                        onValueChange = { selectedHour = it },
                        range = 0..23,
                        modifier = Modifier.width(80.dp)
                    )

                    Text(
                        text = ":",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    // Minute selector
                    NumberPicker(
                        value = selectedMinute,
                        onValueChange = { selectedMinute = it },
                        range = 0..59,
                        modifier = Modifier.width(80.dp)
                    )
                }

                // Display selected time
                Text(
                    text = formatTimeForDisplay(
                        "${selectedHour.toString().padStart(2, '0')}:${selectedMinute.toString().padStart(2, '0')}"
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val hour = selectedHour.toString().padStart(2, '0')
                            val minute = selectedMinute.toString().padStart(2, '0')
                            onConfirm("$hour:$minute")
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("OK")
                    }
                }
            }
        }
    }
}

@Composable
fun NumberPicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    modifier: Modifier = Modifier
) {
    val items = range.toList()
    val listState = rememberLazyListState()

    LaunchedEffect(value) {
        val index = items.indexOf(value)
        if (index >= 0) {
            listState.animateScrollToItem(maxOf(0, index - 1))
        }
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.height(120.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(items) { item ->
                val isSelected = item == value
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onValueChange(item) }
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.toString().padStart(2, '0'),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

fun formatTimeForDisplay(time: String): String {
    val parts = time.split(":")
    if (parts.size != 2) return time

    val hour = parts[0].toIntOrNull() ?: return time
    val minute = parts[1]

    val period = if (hour < 12) "AM" else "PM"
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }

    return "$displayHour:$minute $period"
}
