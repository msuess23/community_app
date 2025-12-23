package com.example.community_app.appointment.domain.usecase.note

import com.example.community_app.appointment.domain.repository.AppointmentNoteRepository

class AddAppointmentNoteUseCase(private val repository: AppointmentNoteRepository) {
  suspend operator fun invoke(appointmentId: Int, text: String, appointmentDate: Long) {
    if (text.isNotBlank()) {
      repository.addNote(appointmentId, text = text.trim(), appointmentDate = appointmentDate)
    }
  }
}