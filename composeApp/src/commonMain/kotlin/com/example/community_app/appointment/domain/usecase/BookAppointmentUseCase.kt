package com.example.community_app.appointment.domain.usecase

import com.example.community_app.appointment.domain.AppointmentRepository
import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.core.domain.calendar.CalendarManager
import com.example.community_app.core.domain.model.Address
import com.example.community_app.core.util.parseIsoToMillis
import com.example.community_app.office.domain.Office
import com.example.community_app.office.domain.OfficeRepository
import kotlinx.coroutines.flow.first

class BookAppointmentUseCase(
  private val appointmentRepository: AppointmentRepository,
  private val officeRepository: OfficeRepository,
  private val calendarManager: CalendarManager
) {
  suspend operator fun invoke(
    officeId: Int,
    slotId: Int,
    addToCalendar: Boolean
  ): Result<Unit, DataError.Remote> {
    val result = appointmentRepository.bookSlot(officeId, slotId)

    if (result is Result.Success && addToCalendar) {
      val appointment = result.data

      val office = officeRepository.getOffice(officeId).first() ?: Office(
        id = officeId, name = "Behörde", description = null,
        services = null, openingHours = null, contactEmail = null,
        phone = null, address = Address(latitude = 0.0, longitude = 0.0)
      )

      val startMillis = parseIsoToMillis(appointment.startsAt)
      val endMillis = parseIsoToMillis(appointment.endsAt)

      val addressStr = "${office.address.street ?: ""} ${office.address.houseNumber ?: ""}, ${office.address.zipCode ?: ""} ${office.address.city ?: ""}"

      val eventId = calendarManager.addEvent(
        title = "Termin: ${office.name}",
        description = "Termin bei ${office.name}.\n${office.services ?: ""}",
        location = addressStr,
        startMillis = startMillis,
        endMillis = endMillis
      )

      if (eventId != null) {
        appointmentRepository.updateCalendarEventId(appointment.id, eventId)
      }
    }

    return when(result) {
      is Result.Success -> Result.Success(Unit)
      is Result.Error -> Result.Error(result.error)
    }
  }
}