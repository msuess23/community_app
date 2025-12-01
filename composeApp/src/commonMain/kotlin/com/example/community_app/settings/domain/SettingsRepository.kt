package com.example.community_app.settings.domain

import com.example.community_app.util.AppLanguage
import com.example.community_app.util.AppTheme
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
  val settings: Flow<AppSettings>

  suspend fun setTheme(theme: AppTheme)
  suspend fun setLanguage(lang: AppLanguage)
  suspend fun setCalendarSyncEnabled(enabled: Boolean)
}