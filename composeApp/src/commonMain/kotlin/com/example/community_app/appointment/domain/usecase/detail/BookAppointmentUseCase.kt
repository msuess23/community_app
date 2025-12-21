package com.example.community_app.appointment.domain.usecase.detail

import com.example.community_app.appointment.domain.AppointmentRepository
import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.core.domain.calendar.CalendarManager
import com.example.community_app.geocoding.domain.Address
import com.example.community_app.core.presentation.state.SyncStatus
import com.example.community_app.core.util.parseIsoToMillis
import com.example.community_app.office.domain.Office
import com.example.community_app.office.domain.OfficeRepository
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.app_title
import community_app.composeapp.generated.resources.appointment_calendar_desc
import community_app.composeapp.generated.resources.appointment_calendar_info
import community_app.composeapp.generated.resources.appointment_singular
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import org.jetbrains.compose.resources.getString

class BookAppointmentUseCase(
  private val appointmentRepository: AppointmentRepository,
  private val officeRepository: OfficeRepository,
  private val calendarManager: CalendarManager,
  private val scheduleAppointmentReminders: ScheduleAppointmentRemindersUseCase
) {
  operator fun invoke(
    officeId: Int,
    slotId: Int,
    addToCalendar: Boolean
  ): Flow<SyncStatus> = flow {
    emit(SyncStatus(isLoading = true))

    val result = appointmentRepository.bookSlot(officeId, slotId)

    var calendarSuccess = true

    if (result is Result.Success && addToCalendar) {
      try {
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

        val addressStr = office.address.let {
          "${it.street ?: ""} ${it.houseNumber ?: ""}, ${it.zipCode ?: ""} ${it.city ?: ""}"
        }.trim()

        val description = "$eventDescription ${office.name}.\n${office.services ?: ""}\n\n$eventBookedVia $appName"

        val eventId = calendarManager.addEvent(
          title = office.name,
          description = description,
          location = addressStr,
          startMillis = startMillis,
          endMillis = endMillis
        )

        if (eventId != null) {
          appointmentRepository.updateCalendarEventId(appointment.id, eventId)
        } else {
          calendarSuccess = false
        }
      } catch (e: Exception) {
        e.printStackTrace()
        calendarSuccess = false
      }
    }

    when(result) {
      is Result.Success -> {
        scheduleAppointmentReminders()

        if (calendarSuccess) {
          emit(SyncStatus(isLoading = false))
        } else {
          emit(SyncStatus(
            isLoading = false,
            error = DataError.Local.CALENDAR_EXPORT_FAILED
          ))
        }
      }
      is Result.Error -> {
        emit(SyncStatus(isLoading = false, error = result.error))
      }
    }
  }
}