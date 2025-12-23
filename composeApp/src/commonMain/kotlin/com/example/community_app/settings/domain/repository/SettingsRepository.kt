package com.example.community_app.settings.domain.repository

import com.example.community_app.settings.domain.model.AppSettings
import com.example.community_app.util.AppLanguage
import com.example.community_app.util.AppTheme
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
  val settings: Flow<AppSettings>

  suspend fun setTheme(theme: AppTheme)
  suspend fun setLanguage(lang: AppLanguage)
  suspend fun setCalendarSyncEnabled(enabled: Boolean)

  suspend fun setNotificationsEnabled(enabled: Boolean)
  suspend fun setNotifyInfos(enabled: Boolean)
  suspend fun setNotifyTickets(enabled: Boolean)
  suspend fun setNotifyAppointments(enabled: Boolean)
  suspend fun setAppointmentReminderOffset(minutes: Int)
}