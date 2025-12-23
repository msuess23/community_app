package com.example.community_app.core.domain.sync

import com.example.community_app.core.domain.location.Location
import com.example.community_app.core.util.GeoUtil
import com.example.community_app.core.util.getCurrentTimeMillis
import com.example.community_app.util.SERVER_FETCH_INTERVAL_MS
import com.example.community_app.util.SERVER_FETCH_RADIUS_KM

object SyncRules {
  fun shouldSync(
    forceRefresh: Boolean,
    lastSyncTime: Long,
    lastSyncLocation: Location?,
    currentLocation: Location?,
    syncIntervalMs: Long = SERVER_FETCH_INTERVAL_MS,
    locationThresholdKm: Double = SERVER_FETCH_RADIUS_KM
  ): Boolean {
    if (forceRefresh) return true

    val now = getCurrentTimeMillis()
    if ((now - lastSyncTime) > syncIntervalMs) return true

    if (currentLocation != null) {
      if (lastSyncLocation == null) return true

      val distance = GeoUtil.calculateDistanceKm(lastSyncLocation, currentLocation)
      if (distance > locationThresholdKm) return true
    }

    return false
  }
}