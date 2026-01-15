package com.enzo.fatesync.presentation.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.enzo.fatesync.presentation.screens.camera.CameraScreen
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
            CameraScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onPhotoCaptured = { photo1Uri, photo2Uri ->
                    val encodedUri1 = Uri.encode(photo1Uri.toString())
                    val encodedUri2 = Uri.encode(photo2Uri.toString())
                    navController.navigate("analysis/$encodedUri1/$encodedUri2")
                }
            )
        }

        composable(
            route = "analysis/{photo1Uri}/{photo2Uri}",
            arguments = listOf(
                navArgument("photo1Uri") { type = NavType.StringType },
                navArgument("photo2Uri") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val photo1Uri = backStackEntry.arguments?.getString("photo1Uri") ?: return@composable
            val photo2Uri = backStackEntry.arguments?.getString("photo2Uri") ?: return@composable
            // AnalysisScreen - to be implemented in Phase 4
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
