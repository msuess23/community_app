package com.example.community_app.appointment.domain.usecase.detail

import com.example.community_app.appointment.domain.AppointmentRepository
import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.core.domain.calendar.CalendarManager
import kotlinx.coroutines.flow.first

class CancelAppointmentUseCase(
  private val repository: AppointmentRepository,
  private val calendarManager: CalendarManager,
  private val scheduleAppointmentReminders: ScheduleAppointmentRemindersUseCase
) {
  suspend operator fun invoke(id: Int): Result<Unit, DataError.Remote> {
    val appointment = repository.getAppointment(id).first()
    val eventId = appointment?.calendarEventId

    val result = repository.cancelAppointment(id)

    if (result is Result.Success) {
      if (eventId != null) calendarManager.removeEvent(eventId)

      scheduleAppointmentReminders.cancelForId(id)
    }

    return result
  }
}