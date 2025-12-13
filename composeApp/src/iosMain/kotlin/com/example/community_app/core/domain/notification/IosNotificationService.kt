package com.example.community_app.core.domain.notification

import platform.Foundation.NSDate
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.timeIntervalSinceDate
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNTimeIntervalNotificationTrigger
import platform.UserNotifications.UNUserNotificationCenter

class IosNotificationService : NotificationService {

  override suspend fun showNotification(id: Int, title: String, message: String) {
    val content = UNMutableNotificationContent()
    content.setTitle(title)
    content.setBody(message)
    content.setSound(UNNotificationSound.defaultSound())

    val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(1.0, repeats = false)

    val request = UNNotificationRequest.requestWithIdentifier(
      identifier = id.toString(),
      content = content,
      trigger = trigger
    )

    addRequest(request)
  }

  override suspend fun scheduleNotification(
    id: Int, title: String, message: String, triggerAtMillis: Long
  ) {
    val content = UNMutableNotificationContent()
    content.setTitle(title)
    content.setBody(message)
    content.setSound(UNNotificationSound.defaultSound())

    val triggerDate = NSDate.dateWithTimeIntervalSince1970(triggerAtMillis / 1000.0)
    val timeInterval = triggerDate.timeIntervalSinceDate(NSDate())

    if (timeInterval <= 0) return

    val trigger = UNTimeIntervalNotificationTrigger.triggerWithTimeInterval(timeInterval, repeats = false)

    val request = UNNotificationRequest.requestWithIdentifier(
      identifier = "reminder_$id",
      content = content,
      trigger = trigger
    )

    addRequest(request)
  }

  override suspend fun cancelScheduledNotification(id: Int) {
    UNUserNotificationCenter.currentNotificationCenter()
      .removePendingNotificationRequestsWithIdentifiers(listOf("reminder_$id"))
  }

  private fun addRequest(request: UNNotificationRequest) {
    UNUserNotificationCenter.currentNotificationCenter().addNotificationRequest(request) { error ->
      if (error != null) println("iOS Notification Error: ${error.localizedDescription}")
    }
  }
}