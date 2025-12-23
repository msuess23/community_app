package com.example.community_app.core.domain.usecase

import com.example.community_app.core.domain.location.Location
import com.example.community_app.core.domain.location.LocationService
import com.example.community_app.core.domain.permission.AppPermissionService

data class LocationResult(
  val permissionGranted: Boolean,
  val location: Location?
)

class FetchUserLocationUseCase(
  private val locationService: LocationService,
  private val permissionService: AppPermissionService
) {
  suspend operator fun invoke(silent: Boolean = false): LocationResult {
    val isGranted = permissionService.isLocationPermissionGranted()

    val hasPermission = if (isGranted) {
      true
    } else if (silent) {
      false
    } else {
      permissionService.requestLocationPermission()
    }

    val location = if (hasPermission) {
      locationService.getCurrentLocation()
    } else {
      null
    }
    return LocationResult(hasPermission, location)
  }
}