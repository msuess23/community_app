package com.example.community_app.core.data.sync

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import com.example.community_app.core.domain.location.Location
import com.example.community_app.core.domain.sync.SyncRules
import com.example.community_app.core.domain.usecase.FetchUserLocationUseCase
import com.example.community_app.core.util.GeoUtil
import com.example.community_app.core.util.getCurrentTimeMillis
import com.example.community_app.util.SERVER_FILTER_RADIUS_KM
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

data class SyncDecision(
  val shouldFetch: Boolean,
  val currentLocation: Location? = null,
  val bboxString: String? = null
)

class SyncManager(
  private val fetchUserLocation: FetchUserLocationUseCase,
  private val dataStore: DataStore<Preferences>
) {
  suspend fun checkSyncStatus(
    featureKey: String,
    forceRefresh: Boolean,
    radiusKm: Double = SERVER_FILTER_RADIUS_KM
  ): SyncDecision {
    val fetchResult = withTimeoutOrNull(3000L) {
      try {
        fetchUserLocation()
      } catch(_: Exception) {
        null
      }
    }

    val currentLocation = fetchResult?.location

    val keyTime = longPreferencesKey("${featureKey}_last_sync_time")
    val keyLat = doublePreferencesKey("${featureKey}_last_sync_lat")
    val keyLng = doublePreferencesKey("${featureKey}_last_sync_lng")

    val prefs = dataStore.data.first()
    val lastSyncTime = prefs[keyTime] ?: 0L

    val lastLat = prefs[keyLat]
    val lastLng = prefs[keyLng]
    val lastSyncLocation = if (lastLat != null && lastLng != null) {
      Location(lastLat, lastLng)
    } else null

    val shouldFetch = SyncRules.shouldSync(
      lastSyncTime = lastSyncTime,
      lastSyncLocation = lastSyncLocation,
      currentLocation = currentLocation,
      forceRefresh = forceRefresh
    )

    val bboxString = if (currentLocation != null) {
      val bbox = GeoUtil.calculateBBox(currentLocation, radiusKm)
      GeoUtil.toBBoxString(bbox)
    } else null

    return SyncDecision(shouldFetch, currentLocation, bboxString)
  }

  suspend fun updateSyncSuccess(featureKey: String, location: Location?) {
    val keyTime = longPreferencesKey("${featureKey}_last_sync_time")
    val keyLat = doublePreferencesKey("${featureKey}_last_sync_lat")
    val keyLng = doublePreferencesKey("${featureKey}_last_sync_lng")

    dataStore.edit { prefs ->
      prefs[keyTime] = getCurrentTimeMillis()
      if (location != null) {
        prefs[keyLat] = location.latitude
        prefs[keyLng] = location.longitude
      }
    }
  }
}