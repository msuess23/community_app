package com.example.community_app.core.util

import android.content.res.Resources
import com.example.community_app.util.AppLanguage
import java.util.Locale

class AndroidLocaleManager : LocaleManager {
  override fun applyLocale(language: AppLanguage) {
    val locale = when (language) {
      AppLanguage.SYSTEM -> getSystemLocale()
      AppLanguage.GERMAN -> Locale.GERMAN
      AppLanguage.ENGLISH -> Locale.ENGLISH
    }
    Locale.setDefault(locale)
  }

  override fun getCurrentLocaleTag(): String {
    return Locale.getDefault().language
  }

  private fun getSystemLocale(): Locale {
    val locales = Resources.getSystem().configuration.locales
    return if (!locales.isEmpty) {
      locales.get(0)
    } else {
      Locale.ENGLISH
    }
  }
}

actual val localeManager: LocaleManager = AndroidLocaleManager()