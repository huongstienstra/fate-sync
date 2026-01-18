package com.enzo.fatesync.presentation.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.enzo.fatesync.domain.model.CompatibilityResult
import com.enzo.fatesync.presentation.screens.camera.CameraScreen
import com.enzo.fatesync.presentation.screens.home.HomeScreen
import com.enzo.fatesync.presentation.screens.result.ResultScreen
import com.enzo.fatesync.presentation.screens.sync.SyncScreen

@Composable
fun NavGraph(navController: NavHostController) {
    var yourPhotoUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var partnerPhotoUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var compatibilityResult by remember { mutableStateOf<CompatibilityResult?>(null) }

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(route = Screen.Home.route) {
            HomeScreen(
                yourPhotoUri = yourPhotoUri,
                partnerPhotoUri = partnerPhotoUri,
                onYourPhotoClick = {
                    navController.navigate("camera/you")
                },
                onPartnerPhotoClick = {
                    navController.navigate("camera/partner")
                },
                onSyncClick = {
                    if (yourPhotoUri != null && partnerPhotoUri != null) {
                        navController.navigate(Screen.Sync.route)
                    }
                }
            )
        }

        composable(
            route = "camera/{type}",
            arguments = listOf(
                navArgument("type") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type") ?: "you"
            val title = if (type == "you") "Your Photo" else "Partner's Photo"

            CameraScreen(
                title = title,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onPhotoCaptured = { uri ->
                    if (type == "you") {
                        yourPhotoUri = uri
                    } else {
                        partnerPhotoUri = uri
                    }
                    navController.popBackStack()
                }
            )
        }

        composable(route = Screen.Sync.route) {
            val yourUri = yourPhotoUri ?: return@composable
            val partnerUri = partnerPhotoUri ?: return@composable

            SyncScreen(
                yourPhotoUri = yourUri,
                partnerPhotoUri = partnerUri,
                onSyncComplete = { result ->
                    compatibilityResult = result
                    navController.navigate(Screen.Result.route) {
                        popUpTo(Screen.Home.route)
                    }
                },
                onError = {
                    navController.popBackStack(Screen.Home.route, inclusive = false)
                }
            )
        }

        composable(route = Screen.Result.route) {
            val yourUri = yourPhotoUri ?: return@composable
            val partnerUri = partnerPhotoUri ?: return@composable
            val result = compatibilityResult ?: return@composable

            ResultScreen(
                yourPhotoUri = yourUri,
                partnerPhotoUri = partnerUri,
                compatibilityResult = result,
                onTryAgain = {
                    yourPhotoUri = null
                    partnerPhotoUri = null
                    compatibilityResult = null
                    navController.popBackStack(Screen.Home.route, inclusive = false)
                },
                onShare = {
                    // Share functionality - to be implemented
                }
            )
        }
    }
}
