package com.enzo.fatesync.presentation.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import com.enzo.fatesync.R
import com.enzo.fatesync.data.local.AppLanguage
import com.enzo.fatesync.data.local.LanguageManager
import com.enzo.fatesync.domain.model.CompatibilityResult
import com.enzo.fatesync.presentation.screens.camera.CameraScreen
import com.enzo.fatesync.presentation.screens.home.HomeScreen
import com.enzo.fatesync.presentation.screens.result.ResultScreen
import com.enzo.fatesync.presentation.screens.sync.SyncScreen
import kotlinx.coroutines.launch

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    languageManager: LanguageManager,
    currentLanguage: AppLanguage
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var yourPhotoUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var partnerPhotoUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var compatibilityResult by remember { mutableStateOf<CompatibilityResult?>(null) }

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(route = Screen.Home.route) {
            HomeScreen(
                yourPhotoUri = yourPhotoUri,
                partnerPhotoUri = partnerPhotoUri,
                currentLanguage = currentLanguage,
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
                },
                onLanguageChange = { language ->
                    scope.launch {
                        languageManager.setLanguage(language)
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
            val title = if (type == "you") {
                context.getString(R.string.camera_title_you)
            } else {
                context.getString(R.string.camera_title_partner)
            }

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
