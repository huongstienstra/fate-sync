package com.enzo.fatesync.presentation.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Camera : Screen("camera")
    data object Analysis : Screen("analysis")
    data object Sync : Screen("sync")
    data object Result : Screen("result")
}
