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

  // Logout
  data object OnLogoutClick : SettingsAction
  data object OnLogoutConfirm : SettingsAction
  data object OnLogoutCancel : SettingsAction
}