package com.enzo.fatesync.data.local

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
private const val PREFS_NAME = "fatesync_prefs"
private const val KEY_LANGUAGE = "app_language"

enum class AppLanguage(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    VIETNAMESE("vi", "Tiếng Việt");

    companion object {
        fun fromCode(code: String): AppLanguage {
            return entries.find { it.code == code } ?: ENGLISH
        }
    }
}

@Singleton
class LanguageManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val languageKey = stringPreferencesKey("app_language")
    private val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    val currentLanguage: Flow<AppLanguage> = context.dataStore.data.map { preferences ->
        // First check AppCompat's current locale (most accurate during runtime)
        val appCompatLocale = AppCompatDelegate.getApplicationLocales()
        if (!appCompatLocale.isEmpty) {
            val code = appCompatLocale.get(0)?.language ?: "en"
            return@map AppLanguage.fromCode(code)
        }
        // Fallback to stored preference
        val code = preferences[languageKey] ?: sharedPrefs.getString(KEY_LANGUAGE, null) ?: Locale.getDefault().language
        AppLanguage.fromCode(code)
    }

    suspend fun setLanguage(language: AppLanguage) {
        // Save to both DataStore and SharedPreferences for persistence across app restarts
        context.dataStore.edit { preferences ->
            preferences[languageKey] = language.code
        }
        sharedPrefs.edit().putString(KEY_LANGUAGE, language.code).apply()

        // Update AppCompat's locale for smooth runtime switching
        val localeList = LocaleListCompat.forLanguageTags(language.code)
        AppCompatDelegate.setApplicationLocales(localeList)
    }

    fun getLocale(language: AppLanguage): Locale {
        return Locale(language.code)
    }
}
