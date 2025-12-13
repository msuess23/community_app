package com.example.community_app.core.domain.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.community_app.core.domain.usecase.CheckStatusUpdatesUseCase
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class StatusUpdateWorker(
  context: Context,
  params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {
  private val checkStatusUpdates: CheckStatusUpdatesUseCase by inject()

  override suspend fun doWork(): Result {
    return try {
      checkStatusUpdates()
      Result.success()
    } catch (e: Exception) {
      e.printStackTrace()
      Result.retry()
    }
  }
}