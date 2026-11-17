package com.shelem.scoresheet.util

import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

object LocaleHelper {

    private const val PREFS_NAME = "shelem_locale_prefs"
    private const val KEY_DEFAULT_LOCALE_INITIALIZED = "default_locale_initialized"
    const val DEFAULT_LANGUAGE = "fa"

    fun isLanguageSwitchSupported(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    }

    private fun isDefaultLocaleInitialized(context: Context): Boolean {
        return try {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_DEFAULT_LOCALE_INITIALIZED, false)
        } catch (_: Exception) {
            false
        }
    }

    private fun markDefaultLocaleInitialized(context: Context) {
        try {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_DEFAULT_LOCALE_INITIALIZED, true)
                .apply()
        } catch (_: Exception) {
        }
    }

    fun ensureDefaultPersianLocale(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val localeManager = context.getSystemService(LocaleManager::class.java)
            if (!isDefaultLocaleInitialized(context)) {
                markDefaultLocaleInitialized(context)
                if (localeManager != null && localeManager.applicationLocales.isEmpty) {
                    Locale.setDefault(Locale.forLanguageTag(DEFAULT_LANGUAGE))
                    localeManager.applicationLocales = LocaleList.forLanguageTags(DEFAULT_LANGUAGE)
                }
            }
        } else {
            Locale.setDefault(Locale.forLanguageTag(DEFAULT_LANGUAGE))
        }
    }

    fun applyLocaleToBaseContext(baseContext: Context): Context {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return createLocalizedContext(baseContext, DEFAULT_LANGUAGE)
        }

        val localeManager = baseContext.getSystemService(LocaleManager::class.java)
        val appLocales = localeManager?.applicationLocales
        if (appLocales != null && !appLocales.isEmpty) {
            Locale.setDefault(appLocales[0])
            return baseContext
        }

        if (!isDefaultLocaleInitialized(baseContext)) {
            return createLocalizedContext(baseContext, DEFAULT_LANGUAGE)
        }

        return baseContext
    }

    fun createLocalizedContext(context: Context, languageTag: String): Context {
        val locale = Locale.forLanguageTag(languageTag)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        return context.createConfigurationContext(config)
    }

    fun getCurrentLanguage(context: Context): String {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return DEFAULT_LANGUAGE
        }
        val localeManager = context.getSystemService(LocaleManager::class.java)
        val appLocales = localeManager?.applicationLocales
        if (appLocales != null && !appLocales.isEmpty) {
            return appLocales[0].language
        }
        if (!isDefaultLocaleInitialized(context)) {
            return DEFAULT_LANGUAGE
        }
        val configLocales = context.resources.configuration.locales
        return if (!configLocales.isEmpty) configLocales[0].language else DEFAULT_LANGUAGE
    }

    fun isPersian(context: Context): Boolean {
        val lang = getCurrentLanguage(context)
        return lang.startsWith("fa", ignoreCase = true)
    }

    fun toggleLanguage(context: Context) {
        if (!isLanguageSwitchSupported()) return
        val currentLang = getCurrentLanguage(context)
        val targetLang = if (currentLang.startsWith("fa", ignoreCase = true)) "en" else "fa"
        setLanguage(context, targetLang)
    }

    fun setLanguage(context: Context, languageTag: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            markDefaultLocaleInitialized(context)
            val localeManager = context.getSystemService(LocaleManager::class.java)
            localeManager?.applicationLocales = LocaleList.forLanguageTags(languageTag)
        }
    }
}
