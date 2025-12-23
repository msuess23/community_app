package com.example.community_app.settings.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.community_app.settings.domain.model.AppSettings
import com.example.community_app.settings.domain.repository.SettingsRepository
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

  private val KEY_NOTIFY_MASTER = booleanPreferencesKey("notify_enabled")
  private val KEY_NOTIFY_INFOS = booleanPreferencesKey("notify_infos")
  private val KEY_NOTIFY_TICKETS = booleanPreferencesKey("notify_tickets")
  private val KEY_NOTIFY_APPOINTMENTS = booleanPreferencesKey("notify_appointments")
  private val KEY_APPOINTMENT_OFFSET = intPreferencesKey("appointment_offset_minutes")

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
      calendarSyncEnabled = prefs[KEY_CALENDAR_SYNC] ?: false,
      notificationsEnabled = prefs[KEY_NOTIFY_MASTER] ?: false,
      notifyInfos = prefs[KEY_NOTIFY_INFOS] ?: true,
      notifyTickets = prefs[KEY_NOTIFY_TICKETS] ?: true,
      notifyAppointments = prefs[KEY_NOTIFY_APPOINTMENTS] ?: true,
      appointmentReminderOffsetMinutes = prefs[KEY_APPOINTMENT_OFFSET] ?: 180
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

  override suspend fun setNotificationsEnabled(enabled: Boolean) {
    dataStore.edit { it[KEY_NOTIFY_MASTER] = enabled }
  }

  override suspend fun setNotifyInfos(enabled: Boolean) {
    dataStore.edit { it[KEY_NOTIFY_INFOS] = enabled }
  }

  override suspend fun setNotifyTickets(enabled: Boolean) {
    dataStore.edit { it[KEY_NOTIFY_TICKETS] = enabled }
  }

  override suspend fun setNotifyAppointments(enabled: Boolean) {
    dataStore.edit { it[KEY_NOTIFY_APPOINTMENTS] = enabled }
  }

  override suspend fun setAppointmentReminderOffset(minutes: Int) {
    dataStore.edit { it[KEY_APPOINTMENT_OFFSET] = minutes }
  }
}