package com.example.community_app.core.domain.permission

import com.example.community_app.core.domain.permission.CalendarPermissionService
import com.example.community_app.core.domain.permission.PermissionStatus
import platform.EventKit.EKEntityType
import platform.EventKit.EKEventStore
import platform.EventKit.EKAuthorizationStatusAuthorized
import platform.EventKit.EKAuthorizationStatusDenied
import platform.EventKit.EKAuthorizationStatusNotDetermined
import platform.EventKit.EKAuthorizationStatusRestricted
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class IosCalendarPermissionService : CalendarPermissionService {
  private val eventStore = EKEventStore()

  override suspend fun checkPermission(): PermissionStatus {
    val status = EKEventStore.authorizationStatusForEntityType(EKEntityType.EKEntityTypeEvent)
    return when (status) {
      EKAuthorizationStatusAuthorized -> PermissionStatus.GRANTED
      EKAuthorizationStatusDenied -> PermissionStatus.DENIED_ALWAYS
      EKAuthorizationStatusRestricted -> PermissionStatus.DENIED_ALWAYS
      EKAuthorizationStatusNotDetermined -> PermissionStatus.NOT_DETERMINED
      else -> PermissionStatus.DENIED
    }
  }

  override suspend fun requestPermission(): PermissionStatus {
    val currentStatus = checkPermission()
    if (currentStatus == PermissionStatus.GRANTED) return PermissionStatus.GRANTED
    if (currentStatus == PermissionStatus.DENIED_ALWAYS) return PermissionStatus.DENIED_ALWAYS

    return suspendCoroutine { continuation ->
      eventStore.requestAccessToEntityType(EKEntityType.EKEntityTypeEvent) { granted, error ->
        if (granted) {
          continuation.resume(PermissionStatus.GRANTED)
        } else {
          continuation.resume(PermissionStatus.DENIED)
        }
      }
    }
  }

  override fun openAppSettings() {
    val url = NSURL.URLWithString(UIApplicationOpenSettingsURLString)
    if (url != null && UIApplication.sharedApplication.canOpenURL(url)) {
      UIApplication.sharedApplication.openURL(url)
    }
  }
}