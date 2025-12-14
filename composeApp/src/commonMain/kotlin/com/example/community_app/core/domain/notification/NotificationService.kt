package com.example.community_app.core.domain.notification

interface NotificationService {
  suspend fun showNotification(
    id: Int,
    title: String,
    message: String
  )

  suspend fun scheduleNotification(
    id: Int,
    title: String,
    message: String,
    triggerAtMillis: Long
  )

  suspend fun cancelScheduledNotification(id: Int)
}