package com.example.community_app.core.domain.location

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse

class IosLocationService : LocationService {
  private val locationManager = CLLocationManager()

  @OptIn(ExperimentalForeignApi::class)
  override suspend fun getCurrentLocation(): Location? {
    val status = CLLocationManager.authorizationStatus()
    val isGranted = status == kCLAuthorizationStatusAuthorizedWhenInUse ||
                    status == kCLAuthorizationStatusAuthorizedAlways

    if (!isGranted) return null

    val location = locationManager.location
    return if (location != null) {
      val coord = location.coordinate
      coord.useContents {
        Location(latitude , longitude)
      }
    } else null
  }
}