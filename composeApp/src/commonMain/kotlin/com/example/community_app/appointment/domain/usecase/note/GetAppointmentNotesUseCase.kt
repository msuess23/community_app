package com.example.community_app.appointment.domain.usecase.note

import com.example.community_app.appointment.domain.repository.AppointmentNoteRepository

class GetAppointmentNotesUseCase(private val repository: AppointmentNoteRepository) {
  operator fun invoke(appointmentId: Int) = repository.getNotesForAppointment(appointmentId)
}