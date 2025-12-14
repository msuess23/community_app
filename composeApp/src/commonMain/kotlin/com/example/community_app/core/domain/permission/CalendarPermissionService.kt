package com.example.community_app.core.domain.permission

enum class PermissionStatus {
  GRANTED, DENIED, DENIED_ALWAYS, NOT_DETERMINED
}

interface CalendarPermissionService {
  suspend fun checkPermission(): PermissionStatus
  suspend fun requestPermission(): PermissionStatus

  fun openAppSettings()
}