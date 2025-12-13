package com.example.community_app.core.domain.notification

import android.R.drawable.ic_dialog_info
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.community_app.MainActivity
import community_app.composeapp.generated.resources.Res
import java.util.concurrent.TimeUnit

class AndroidNotificationService(
  private val context: Context
) : NotificationService {

  companion object {
    private const val CHANNEL_ID = "community_updates"
  }

  init {
    createNotificationChannel()
  }

  override suspend fun showNotification(id: Int, title: String, message: String) {
    val intent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    val pendingIntent = PendingIntent.getActivity(
      context, 0, intent, PendingIntent.FLAG_IMMUTABLE
    )

    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
      .setSmallIcon(ic_dialog_info) // TODO: R.drawable.ic_notification
      .setContentTitle(title)
      .setContentText(message)
      .setPriority(NotificationCompat.PRIORITY_DEFAULT)
      .setContentIntent(pendingIntent)
      .setAutoCancel(true)

    try {
      NotificationManagerCompat.from(context).notify(id, builder.build())
    } catch (e: SecurityException) {
      e.printStackTrace()
    }
  }

  override suspend fun scheduleNotification(
    id: Int, title: String, message: String, triggerAtMillis: Long
  ) {
    val delay = triggerAtMillis - System.currentTimeMillis()
    if (delay <= 0) return

    val data = workDataOf(
      "id" to id,
      "title" to title,
      "message" to message
    )

    val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
      .setInitialDelay(delay, TimeUnit.MILLISECONDS)
      .setInputData(data)
      .addTag("reminder_$id")
      .build()

    WorkManager.getInstance(context).enqueueUniqueWork(
      "reminder_$id",
      androidx.work.ExistingWorkPolicy.REPLACE,
      workRequest
    )
  }

  override suspend fun cancelScheduledNotification(id: Int) {
    WorkManager.getInstance(context).cancelUniqueWork("reminder_$id")
  }

  private fun createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val importance = NotificationManager.IMPORTANCE_DEFAULT

      val name = context.getString(Res.string.notification_channel_name)
      val descriptionText = context.getString(Res.string.notification_channel_description)

      val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
        description = descriptionText
      }
      val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      manager.createNotificationChannel(channel)
    }
  }
}