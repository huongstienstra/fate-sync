package com.enzo.fatesync.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.enzo.fatesync.presentation.screens.home.HomeScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(route = Screen.Home.route) {
            HomeScreen(
                onNavigateToCamera = {
                    navController.navigate(Screen.Camera.route)
                }
            )
        }

        composable(route = Screen.Camera.route) {
            // CameraScreen - to be implemented
        }

        composable(route = Screen.Analysis.route) {
            // AnalysisScreen - to be implemented
        }

        composable(
            route = Screen.Result.route,
            arguments = listOf(
                navArgument("resultId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val resultId = backStackEntry.arguments?.getString("resultId") ?: return@composable
            // ResultScreen - to be implemented
        }
    }
}
