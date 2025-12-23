package com.example.community_app.appointment.domain.usecase.note

import com.example.community_app.appointment.domain.repository.AppointmentNoteRepository

class DeleteAppointmentNoteUseCase(private val repository: AppointmentNoteRepository) {
  suspend operator fun invoke(noteId: Int) = repository.deleteNote(noteId)
}