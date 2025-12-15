package com.example.community_app.settings.presentation

import com.example.community_app.settings.domain.AppSettings
import com.example.community_app.util.AppLanguage
import com.example.community_app.util.AppTheme

data class SettingsState(
  val settings: AppSettings = AppSettings(
    theme = AppTheme.SYSTEM,
    language = AppLanguage.SYSTEM,
    calendarSyncEnabled = false,
    notificationsEnabled = false,
    notifyInfos = true,
    notifyTickets = true,
    notifyAppointments = true,
    appointmentReminderOffsetMinutes = 180
  ),
  val selectedTabIndex: Int = 0,
  val pendingLanguage: AppLanguage? = null,
  val isLoading: Boolean = false,
  val showCalendarPermissionRationale: Boolean = false,
)