package com.example.community_app.core.util

import com.example.community_app.util.AppLanguage
import platform.Foundation.NSLocale
import platform.Foundation.NSUserDefaults
import platform.Foundation.preferredLanguages

class IosLocaleManager : LocaleManager {
  var currentCode: String? = null

  override fun applyLocale(language: AppLanguage) {
    val code = when (language) {
      AppLanguage.SYSTEM -> null
      AppLanguage.GERMAN -> "de"
      AppLanguage.ENGLISH -> "en"
    }
    currentCode = code

    val defaults = NSUserDefaults.standardUserDefaults
    if (code != null) {
      defaults.setObject(listOf(code), forKey = "AppleLanguages")
    } else {
      defaults.removeObjectForKey("AppleLanguages")
    }
    defaults.synchronize()
  }

  override fun getCurrentLocaleTag(): String {
    return currentCode ?: (NSLocale.preferredLanguages.firstOrNull() as? String) ?: "en"
  }
}

actual val localeManager: LocaleManager = IosLocaleManager()