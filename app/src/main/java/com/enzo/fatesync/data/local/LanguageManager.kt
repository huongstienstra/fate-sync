package com.enzo.fatesync.data.local

import android.content.Context
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
        val code = preferences[languageKey] ?: sharedPrefs.getString(KEY_LANGUAGE, null) ?: Locale.getDefault().language
        AppLanguage.fromCode(code)
    }

    suspend fun setLanguage(language: AppLanguage) {
        // Save to both DataStore and SharedPreferences for reliability
        context.dataStore.edit { preferences ->
            preferences[languageKey] = language.code
        }
        sharedPrefs.edit().putString(KEY_LANGUAGE, language.code).apply()
    }

    fun getLocale(language: AppLanguage): Locale {
        return Locale(language.code)
    }
}
