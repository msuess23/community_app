package com.example.community_app.core.domain.permission

interface AppPermissionService {
  suspend fun requestLocationPermission(): Boolean
  suspend fun isLocationPermissionGranted() : Boolean

  suspend fun requestNotificationPermission(): Boolean
  suspend fun isNotificationPermissionGranted(): Boolean
}