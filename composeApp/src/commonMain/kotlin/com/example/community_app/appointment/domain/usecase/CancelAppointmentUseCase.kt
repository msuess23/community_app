package com.example.community_app.appointment.domain.usecase

import com.example.community_app.appointment.domain.AppointmentRepository
import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.core.domain.calendar.CalendarManager
import kotlinx.coroutines.flow.first

class CancelAppointmentUseCase(
  private val repository: AppointmentRepository,
  private val calendarManager: CalendarManager
) {
  suspend operator fun invoke(id: Int): Result<Unit, DataError.Remote> {
    val appointment = repository.getAppointment(id).first()
    val eventId = appointment?.calendarEventId

    val result = repository.cancelAppointment(id)

    if (result is Result.Success && eventId != null) {
      calendarManager.removeEvent(eventId)
    }

    return result
  }
}