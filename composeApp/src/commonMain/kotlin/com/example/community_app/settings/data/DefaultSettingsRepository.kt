package com.example.community_app.settings.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.community_app.settings.domain.AppSettings
import com.example.community_app.settings.domain.SettingsRepository
import com.example.community_app.util.AppLanguage
import com.example.community_app.util.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DefaultSettingsRepository(
  private val dataStore: DataStore<Preferences>
) : SettingsRepository {
  private val KEY_THEME = stringPreferencesKey("theme")
  private val KEY_LANGUAGE = stringPreferencesKey("language")
  private val KEY_CALENDAR_SYNC = booleanPreferencesKey("calendar_sync_enabled")

  override val settings: Flow<AppSettings> = dataStore.data.map { prefs ->
    AppSettings(
      theme = try {
        AppTheme.valueOf(prefs[KEY_THEME] ?: AppTheme.SYSTEM.name)
      } catch (e: Exception) {
        AppTheme.SYSTEM
      },
      language = try {
        AppLanguage.valueOf(prefs[KEY_LANGUAGE] ?: AppLanguage.SYSTEM.name)
      } catch (e: Exception) {
        AppLanguage.SYSTEM
      },
      calendarSyncEnabled = prefs[KEY_CALENDAR_SYNC] ?: false
    )
  }

  override suspend fun setTheme(theme: AppTheme) {
    dataStore.edit { prefs ->
      prefs[KEY_THEME] = theme.name
    }
  }

  override suspend fun setLanguage(lang: AppLanguage) {
    dataStore.edit { prefs ->
      prefs[KEY_LANGUAGE] = lang.name
    }
  }

  override suspend fun setCalendarSyncEnabled(enabled: Boolean) {
    dataStore.edit { prefs -> prefs[KEY_CALENDAR_SYNC] = enabled }
  }
}