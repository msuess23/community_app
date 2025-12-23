package com.example.community_app.settings.domain.model

import com.example.community_app.util.AppLanguage
import com.example.community_app.util.AppTheme

data class AppSettings(
  val theme: AppTheme,
  val language: AppLanguage,
  val calendarSyncEnabled: Boolean,
  val notificationsEnabled: Boolean = false,
  val notifyInfos: Boolean = true,
  val notifyTickets: Boolean = true,
  val notifyAppointments: Boolean = true,
  val appointmentReminderOffsetMinutes: Int = 180
)