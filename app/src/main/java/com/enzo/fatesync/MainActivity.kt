package com.enzo.fatesync

import android.content.Context
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.os.LocaleListCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import com.enzo.fatesync.data.local.AppLanguage
import com.enzo.fatesync.data.local.LanguageManager
import com.enzo.fatesync.data.local.LocaleHelper
import com.enzo.fatesync.presentation.navigation.NavGraph
import com.enzo.fatesync.ui.theme.FateSyncTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var languageManager: LanguageManager

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(newBase)

        // Initialize AppCompat locale from our persisted preference on first launch
        if (AppCompatDelegate.getApplicationLocales().isEmpty) {
            val locale = LocaleHelper.getPersistedLocale(newBase)
            val language = AppLanguage.fromCode(locale.language)
            val localeList = LocaleListCompat.forLanguageTags(language.code)
            AppCompatDelegate.setApplicationLocales(localeList)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Get current locale from AppCompat
            val appCompatLocale = AppCompatDelegate.getApplicationLocales()
            val initialLanguage = if (!appCompatLocale.isEmpty) {
                AppLanguage.fromCode(appCompatLocale.get(0)?.language ?: "en")
            } else {
                AppLanguage.ENGLISH
            }
            val currentLanguage by languageManager.currentLanguage.collectAsState(initial = initialLanguage)

            FateSyncTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavGraph(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding),
                        languageManager = languageManager,
                        currentLanguage = currentLanguage
                    )
                }
            }
        }
    }
}
