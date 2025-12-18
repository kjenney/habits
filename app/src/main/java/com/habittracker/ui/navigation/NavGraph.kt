package com.habittracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.habittracker.ui.screens.AddEditHabitScreen
import com.habittracker.ui.screens.ExportScreen
import com.habittracker.ui.screens.HabitDetailScreen
import com.habittracker.ui.screens.HomeScreen
import com.habittracker.ui.viewmodel.ExportViewModel
import com.habittracker.ui.viewmodel.HabitViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    habitViewModel: HabitViewModel = viewModel(),
    exportViewModel: ExportViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.Home.route
    ) {
        composable(NavRoutes.Home.route) {
            HomeScreen(
                viewModel = habitViewModel,
                onAddHabit = { navController.navigate(NavRoutes.AddHabit.route) },
                onHabitClick = { habitId ->
                    navController.navigate(NavRoutes.HabitDetail.createRoute(habitId))
                },
                onEditHabit = { habitId ->
                    navController.navigate(NavRoutes.EditHabit.createRoute(habitId))
                },
                onExportClick = { navController.navigate(NavRoutes.Export.route) }
            )
        }

        composable(NavRoutes.AddHabit.route) {
            AddEditHabitScreen(
                viewModel = habitViewModel,
                habitId = null,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = NavRoutes.EditHabit.route,
            arguments = listOf(navArgument("habitId") { type = NavType.LongType })
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getLong("habitId")
            AddEditHabitScreen(
                viewModel = habitViewModel,
                habitId = habitId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = NavRoutes.HabitDetail.route,
            arguments = listOf(navArgument("habitId") { type = NavType.LongType })
        ) { backStackEntry ->
            val habitId = backStackEntry.arguments?.getLong("habitId") ?: return@composable
            HabitDetailScreen(
                viewModel = habitViewModel,
                habitId = habitId,
                onNavigateBack = { navController.popBackStack() },
                onEditClick = { navController.navigate(NavRoutes.EditHabit.createRoute(habitId)) }
            )
        }

        composable(NavRoutes.Export.route) {
            ExportScreen(
                viewModel = exportViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
