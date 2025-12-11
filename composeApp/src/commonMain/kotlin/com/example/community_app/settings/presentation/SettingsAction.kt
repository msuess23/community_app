package com.example.community_app.settings.presentation

import com.example.community_app.util.AppLanguage
import com.example.community_app.util.AppTheme

sealed interface SettingsAction {
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

}