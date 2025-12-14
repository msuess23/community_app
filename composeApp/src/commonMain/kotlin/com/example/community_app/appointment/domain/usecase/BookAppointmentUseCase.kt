package com.example.community_app.appointment.domain.usecase

import com.example.community_app.appointment.domain.AppointmentRepository
import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.core.domain.calendar.CalendarManager
import com.example.community_app.core.domain.model.Address
import com.example.community_app.core.util.parseIsoToMillis
import com.example.community_app.office.domain.Office
import com.example.community_app.office.domain.OfficeRepository
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.app_title
import community_app.composeapp.generated.resources.appointment_calendar_desc
import community_app.composeapp.generated.resources.appointment_calendar_info
import community_app.composeapp.generated.resources.appointment_singular
import kotlinx.coroutines.flow.first
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

class BookAppointmentUseCase(
  private val appointmentRepository: AppointmentRepository,
  private val officeRepository: OfficeRepository,
  private val calendarManager: CalendarManager,
  private val scheduleAppointmentReminders: ScheduleAppointmentRemindersUseCase
) {
  suspend operator fun invoke(
    officeId: Int,
    slotId: Int,
    addToCalendar: Boolean
  ): Result<Unit, DataError.Remote> {
    val result = appointmentRepository.bookSlot(officeId, slotId)

    if (result is Result.Success && addToCalendar) {
      val appointment = result.data

      val titleDefault = getString(Res.string.appointment_singular)
      val eventDescription = getString(Res.string.appointment_calendar_desc)
      val eventBookedVia = getString(Res.string.appointment_calendar_info)
      val appName = getString(Res.string.app_title)

      val office = officeRepository.getOffice(officeId).first() ?: Office(
        id = officeId, name = titleDefault, description = null,
        services = null, openingHours = null, contactEmail = null,
        phone = null, address = Address(latitude = 0.0, longitude = 0.0)
      )

      val startMillis = parseIsoToMillis(appointment.startsAt)
      val endMillis = parseIsoToMillis(appointment.endsAt)

      val addressStr = "${office.address.street ?: ""} ${office.address.houseNumber ?: ""}, ${office.address.zipCode ?: ""} ${office.address.city ?: ""}"

      val eventId = calendarManager.addEvent(
        title = office.name,
        description = "$eventDescription ${office.name}.\n${office.services ?: ""}\n\n$eventBookedVia $appName",
        location = addressStr,
        startMillis = startMillis,
        endMillis = endMillis
      )

      if (eventId != null) {
        appointmentRepository.updateCalendarEventId(appointment.id, eventId)
      }
    }

    return when(result) {
      is Result.Success -> {
        scheduleAppointmentReminders()
        Result.Success(Unit)
      }
      is Result.Error -> Result.Error(result.error)
    }
  }
}