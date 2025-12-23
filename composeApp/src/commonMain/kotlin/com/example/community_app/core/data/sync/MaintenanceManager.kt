package com.example.community_app.core.data.sync

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import com.example.community_app.appointment.domain.usecase.note.DeleteOutdatedNotesUseCase
import com.example.community_app.core.util.getCurrentTimeMillis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MaintenanceManager(
  private val deleteOutdatedNotes: DeleteOutdatedNotesUseCase,
  private val dataStore: DataStore<Preferences>
) {
  private val KEY_LAST_MAINTENANCE = longPreferencesKey("last_db_maintenance_time")

  suspend fun tryRunDailyMaintenance() {
    val prefs = dataStore.data.first()
    val lastMaintenance = prefs[KEY_LAST_MAINTENANCE] ?: 0L
    val now = getCurrentTimeMillis()

    val oneDayMs = 24 * 60 * 60 * 1000L

    if (now - lastMaintenance > oneDayMs) {
      CoroutineScope(Dispatchers.IO).launch {
        try {
          deleteOutdatedNotes()
          dataStore.edit { it[KEY_LAST_MAINTENANCE] = now }
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
    }
  }
}