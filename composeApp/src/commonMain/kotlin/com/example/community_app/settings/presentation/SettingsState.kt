package com.example.community_app.settings.presentation

import com.example.community_app.settings.domain.AppSettings
import com.example.community_app.util.AppLanguage
import com.example.community_app.util.AppTheme

data class SettingsState(
  val settings: AppSettings = AppSettings(
    theme = AppTheme.SYSTEM,
    language = AppLanguage.SYSTEM
  ),
  val showLogoutDialog: Boolean = false,
  val pendingLanguage: AppLanguage? = null,
  val isLoading: Boolean = false,
  val showPasswordResetDialog: Boolean = false,
  val currentUserEmail: String? = null
)