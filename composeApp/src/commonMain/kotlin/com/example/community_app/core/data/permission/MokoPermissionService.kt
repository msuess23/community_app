package com.example.community_app.core.data.permission

import com.example.community_app.core.domain.permission.AppPermissionService
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController

class MokoPermissionService(
  private val controller: PermissionsController
) : AppPermissionService {
  override suspend fun requestLocationPermission(): Boolean {
    return try {
      controller.providePermission(Permission.LOCATION)
      true
    } catch (e: Exception) {
      false
    }
  }

  override suspend fun isLocationPermissionGranted(): Boolean {
    return controller.isPermissionGranted(Permission.LOCATION)
  }

  override suspend fun requestNotificationPermission(): Boolean {
    return try {
      controller.providePermission(Permission.REMOTE_NOTIFICATION)
      true
    } catch (e: Exception) {
      false
    }
  }

  override suspend fun isNotificationPermissionGranted(): Boolean {
    return controller.isPermissionGranted(Permission.REMOTE_NOTIFICATION)
  }
}