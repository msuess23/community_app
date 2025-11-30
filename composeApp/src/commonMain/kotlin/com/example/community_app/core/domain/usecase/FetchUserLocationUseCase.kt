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
  suspend operator fun invoke(): LocationResult {
    val hasPermission = permissionService.requestLocationPermission()
    val location = if (hasPermission) {
      locationService.getCurrentLocation()
    } else {
      null
    }
    return LocationResult(hasPermission, location)
  }
}