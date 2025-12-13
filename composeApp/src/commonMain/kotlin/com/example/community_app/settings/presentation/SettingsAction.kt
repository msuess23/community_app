package com.example.community_app.settings.presentation

import com.example.community_app.ticket.presentation.ticket_master.TicketMasterAction
import com.example.community_app.util.AppLanguage
import com.example.community_app.util.AppTheme

sealed interface SettingsAction {
  data class OnTabChange(val index: Int) : SettingsAction

  // Theme
  data class OnThemeChange(val theme: AppTheme) : SettingsAction

  // Language
  data class OnLanguageSelect(val language: AppLanguage) : SettingsAction
  data object OnLanguageConfirm : SettingsAction
  data object OnLanguageDismiss : SettingsAction

  // Calendar Sync
  data class OnToggleCalendarSync(val enabled: Boolean) : SettingsAction
  data object OnResume : SettingsAction
  data object OnOpenSettings : SettingsAction

  // Log in/out
  data object OnLoginClick : SettingsAction
  data object OnLogoutClick : SettingsAction
  data object OnLogoutConfirm : SettingsAction
  data object OnLogoutCancel : SettingsAction

  // Change password
  data object OnChangePasswordClick : SettingsAction
  data object OnChangePasswordDismiss : SettingsAction
  data object OnChangePasswordConfirm : SettingsAction

  // Notifications
  data class OnToggleNotifications(val enabled: Boolean) : SettingsAction
  data class OnToggleNotifyTickets(val enabled: Boolean) : SettingsAction
  data class OnToggleNotifyInfos(val enabled: Boolean) : SettingsAction
  data class OnToggleNotifyAppointments(val enabled: Boolean) : SettingsAction
  data class OnChangeAppointmentReminderOffset(val minutes: Int) : SettingsAction
}