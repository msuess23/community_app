package com.example.community_app.core.util

import com.example.community_app.util.AppLanguage

interface LocaleManager {
  fun applyLocale(language: AppLanguage)
  fun getCurrentLocaleTag(): String
}

expect val localeManager: LocaleManager