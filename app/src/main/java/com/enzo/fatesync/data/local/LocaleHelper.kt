package com.enzo.fatesync.data.local

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

private const val PREFS_NAME = "fatesync_prefs"
private const val KEY_LANGUAGE = "app_language"

object LocaleHelper {

    fun setLocale(context: Context, language: AppLanguage): Context {
        return updateResources(context, language.code)
    }

    private fun updateResources(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        return context.createConfigurationContext(config)
    }

    fun getPersistedLocale(context: Context): Locale {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val languageCode = prefs.getString(KEY_LANGUAGE, null) ?: Locale.getDefault().language
        return Locale(languageCode)
    }
}
