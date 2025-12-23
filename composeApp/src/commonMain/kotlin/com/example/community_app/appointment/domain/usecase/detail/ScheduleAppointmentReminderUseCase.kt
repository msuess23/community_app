package com.example.community_app.appointment.domain.usecase.detail

import com.example.community_app.appointment.domain.repository.AppointmentRepository
import com.example.community_app.core.domain.notification.NotificationService
import com.example.community_app.core.util.getCurrentTimeMillis
import com.example.community_app.core.util.parseIsoToMillis
import com.example.community_app.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first

class ScheduleAppointmentRemindersUseCase(
  private val appointmentRepository: AppointmentRepository,
  private val settingsRepository: SettingsRepository,
  private val notificationService: NotificationService
) {
  suspend operator fun invoke() {
    val settings = settingsRepository.settings.first()

    if (!settings.notificationsEnabled || !settings.notifyAppointments) {
      return
    }

    val offsetMs = settings.appointmentReminderOffsetMinutes * 60 * 1000L

    val appointments = appointmentRepository.getAppointments().first()
    val now = getCurrentTimeMillis()

    appointments.forEach { appointment ->
      val startMillis = parseIsoToMillis(appointment.startsAt)
      val triggerTime = startMillis - offsetMs

      if (triggerTime > now) {
        notificationService.scheduleNotification(
          id = appointment.id,
          title = "Terminerinnerung",
          message = "Dein Termin beginnt um ${appointment.startsAt.substring(11, 16)} Uhr.",
          triggerAtMillis = triggerTime
        )
      }
    }
  }

  suspend fun cancelForId(appointmentId: Int) {
    notificationService.cancelScheduledNotification(appointmentId)
  }
}