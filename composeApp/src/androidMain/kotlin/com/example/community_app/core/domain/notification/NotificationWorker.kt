package com.example.community_app.core.domain.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class NotificationWorker(
  context: Context,
  params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {
  private val notificationService: NotificationService by inject()

  override suspend fun doWork(): Result {
    val id = inputData.getInt("id", 0)
    val title = inputData.getString("title") ?: return Result.failure()
    val message = inputData.getString("message") ?: return Result.failure()

    notificationService.showNotification(id, title, message)
    return Result.success()
  }
}