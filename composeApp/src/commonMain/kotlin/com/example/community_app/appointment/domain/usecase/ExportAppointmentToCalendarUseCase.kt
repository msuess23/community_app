package com.example.community_app.appointment.domain.usecase

import com.example.community_app.appointment.domain.Appointment
import com.example.community_app.core.domain.calendar.CalendarService
import com.example.community_app.core.domain.permission.AppPermissionService
import com.example.community_app.core.util.parseIsoToMillis
import com.example.community_app.office.domain.OfficeRepository
import kotlinx.coroutines.flow.first

class ExportAppointmentToCalendarUseCase(
  private val calendarService: CalendarService,
  private val officeRepository: OfficeRepository,
  private val permissionService: AppPermissionService
) {
  suspend operator fun invoke(appointment: Appointment): Boolean {
    val hasPermission = if (permissionService.isCalendarPermissionGranted()) {
      true
    } else {
      permissionService.requestCalendarPermission()
    }

    if (!hasPermission) return false

    val office = officeRepository.getOffice(appointment.officeId).first()

    val title = "Termin: ${office?.name ?: "Behörde"}"
    val description = "Termin bei ${office?.name}. ${office?.services ?: ""}"
    val location = office?.address?.let { "${it.street} ${it.houseNumber}, ${it.city}" }

    val startMillis = parseIsoToMillis(appointment.startsAt)
    val endMillis = parseIsoToMillis(appointment.endsAt)

    if (startMillis == 0L || endMillis == 0L) return false

    return calendarService.addEvent(
      title = title,
      description = description,
      location = location,
      startMillis = startMillis,
      endMillis = endMillis
    )
  }
}