package com.enzo.fatesync.presentation.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Camera : Screen("camera")
    data object Analysis : Screen("analysis")
    data object Result : Screen("result/{resultId}") {
        fun createRoute(resultId: String) = "result/$resultId"
    }
}
