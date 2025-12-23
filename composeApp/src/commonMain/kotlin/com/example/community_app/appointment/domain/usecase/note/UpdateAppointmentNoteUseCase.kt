package com.example.community_app.appointment.domain.usecase.note

import com.example.community_app.appointment.domain.repository.AppointmentNoteRepository

class UpdateAppointmentNoteUseCase(private val repository: AppointmentNoteRepository) {
  suspend operator fun invoke(noteId: Int, newText: String) {
    if (newText.isNotBlank()) {
      repository.updateNote(noteId, newText.trim())
    }
  }
}