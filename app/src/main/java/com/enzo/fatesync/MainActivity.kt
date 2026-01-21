package com.enzo.fatesync

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.enzo.fatesync.data.local.AppLanguage
import com.enzo.fatesync.data.local.LanguageManager
import com.enzo.fatesync.data.local.LocaleHelper
import com.enzo.fatesync.presentation.navigation.NavGraph
import com.enzo.fatesync.ui.theme.FateSyncTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var languageManager: LanguageManager

    private var currentLocaleCode: String? = null

    override fun attachBaseContext(newBase: Context) {
        val locale = LocaleHelper.getPersistedLocale(newBase)
        val language = AppLanguage.fromCode(locale.language)
        super.attachBaseContext(LocaleHelper.setLocale(newBase, language))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val currentLanguage by languageManager.currentLanguage.collectAsState(initial = AppLanguage.ENGLISH)

            // Recreate activity when language changes
            LaunchedEffect(currentLanguage) {
                if (currentLocaleCode != null && currentLocaleCode != currentLanguage.code) {
                    recreate()
                }
                currentLocaleCode = currentLanguage.code
            }

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