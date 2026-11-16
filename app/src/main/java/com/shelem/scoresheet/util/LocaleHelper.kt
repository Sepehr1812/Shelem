package com.shelem.scoresheet.util

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import java.util.Locale

object LocaleHelper {

    fun getCurrentLanguage(context: Context): String {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val localeManager = context.getSystemService(LocaleManager::class.java)
            val appLocales = localeManager?.applicationLocales
            if (appLocales != null && !appLocales.isEmpty) {
                return appLocales[0].language
            }
        }
        val configLocales = context.resources.configuration.locales
        return if (!configLocales.isEmpty) configLocales[0].language else Locale.getDefault().language
    }

    fun isPersian(context: Context): Boolean {
        val lang = getCurrentLanguage(context)
        return lang.startsWith("fa", ignoreCase = true)
    }

    fun toggleLanguage(context: Context) {
        val currentLang = getCurrentLanguage(context)
        val targetLang = if (currentLang.startsWith("fa", ignoreCase = true)) "en" else "fa"
        setLanguage(context, targetLang)
    }

    fun setLanguage(context: Context, languageTag: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val localeManager = context.getSystemService(LocaleManager::class.java)
            localeManager?.applicationLocales = LocaleList.forLanguageTags(languageTag)
        }
    }
}
