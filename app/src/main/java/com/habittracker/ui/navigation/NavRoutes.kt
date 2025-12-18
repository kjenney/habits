package com.habittracker.ui.navigation

sealed class NavRoutes(val route: String) {
    object Home : NavRoutes("home")
    object AddHabit : NavRoutes("add_habit")
    object EditHabit : NavRoutes("edit_habit/{habitId}") {
        fun createRoute(habitId: Long) = "edit_habit/$habitId"
    }
    object HabitDetail : NavRoutes("habit_detail/{habitId}") {
        fun createRoute(habitId: Long) = "habit_detail/$habitId"
    }
    object Export : NavRoutes("export")
}
