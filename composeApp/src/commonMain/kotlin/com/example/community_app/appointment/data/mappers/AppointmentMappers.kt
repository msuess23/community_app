package com.example.community_app.appointment.data.mappers

import com.example.community_app.appointment.data.local.AppointmentEntity
import com.example.community_app.appointment.domain.Appointment
import com.example.community_app.dto.AppointmentDto

fun AppointmentDto.toEntity(): AppointmentEntity {
  return AppointmentEntity(
    id = id,
    officeId = officeId,
    userId = userId,
    startsAt = startsAt,
    endsAt = endsAt
  )
}

fun AppointmentEntity.toAppointment(): Appointment {
  return Appointment(
    id = id,
    officeId = officeId,
    userId = userId,
    startsAt = startsAt,
    endsAt = endsAt
  )
}