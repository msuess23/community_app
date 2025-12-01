package com.example.community_app.settings.domain

import com.example.community_app.util.AppLanguage
import com.example.community_app.util.AppTheme

data class AppSettings(
  val theme: AppTheme,
  val language: AppLanguage,
  val calendarSyncEnabled: Boolean
)