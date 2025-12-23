package com.example.community_app.appointment.domain.usecase.master

import com.example.community_app.appointment.domain.model.Appointment
import com.example.community_app.appointment.presentation.master.AppointmentFilterState
import com.example.community_app.appointment.presentation.master.AppointmentSortOption
import com.example.community_app.core.util.addDays
import com.example.community_app.core.util.getStartOfDay
import com.example.community_app.core.util.parseIsoToMillis

class FilterAppointmentsUseCase {
  operator fun invoke(
    appointments: List<Appointment>,
    filter: AppointmentFilterState
  ): List<Appointment> {
    var result = appointments

    if (filter.startDate != null || filter.endDate != null) {
      val minMillis = filter.startDate?.let { getStartOfDay(it) }
      val maxMillis = filter.endDate?.let {
        val startOfDay = getStartOfDay(it)
        addDays(startOfDay, 1)
      }

      result = result.filter { appointment ->
        try {
          val appointmentStart = parseIsoToMillis(appointment.startsAt)

          val startCondition = if (minMillis != null) {
            appointmentStart >= minMillis
          } else true

          val endCondition = if (maxMillis != null) {
            appointmentStart <= maxMillis
          } else true

          startCondition && endCondition
        } catch (_: Exception) {
          true
        }
      }
    }

    result = when(filter.sortOption) {
      AppointmentSortOption.DATE_ASC -> result.sortedBy { it.startsAt }
      AppointmentSortOption.DATE_DESC -> result.sortedByDescending { it.startsAt }
    }

    return result
  }
}