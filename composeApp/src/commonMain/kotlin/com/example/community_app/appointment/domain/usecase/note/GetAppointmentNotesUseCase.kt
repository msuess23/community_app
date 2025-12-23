package com.example.community_app.appointment.domain.usecase.note

import com.example.community_app.appointment.domain.repository.AppointmentNoteRepository
import kotlinx.coroutines.flow.map

class GetAppointmentNotesUseCase(private val repository: AppointmentNoteRepository) {
  operator fun invoke(appointmentId: Int) = repository.getNotesForAppointment(appointmentId)
    .map { notes ->
      notes.sortedByDescending { it.createdAt }
    }
}