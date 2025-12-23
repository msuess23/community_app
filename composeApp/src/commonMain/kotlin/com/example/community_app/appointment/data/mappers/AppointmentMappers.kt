package com.example.community_app.appointment.data.mappers

import com.example.community_app.appointment.data.local.appointment.AppointmentEntity
import com.example.community_app.appointment.domain.model.Appointment
import com.example.community_app.dto.AppointmentDto

fun AppointmentDto.toEntity(calendarEventId: String? = null): AppointmentEntity {
  return AppointmentEntity(
    id = id,
    officeId = officeId,
    userId = userId,
    startsAt = startsAt,
    endsAt = endsAt,
    calendarEventId = calendarEventId
  )
}

fun AppointmentEntity.toAppointment(): Appointment {
  return Appointment(
    id = id,
    officeId = officeId,
    userId = userId,
    startsAt = startsAt,
    endsAt = endsAt,
    calendarEventId = calendarEventId
  )
}