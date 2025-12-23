package com.example.community_app.appointment.domain.repository

import com.example.community_app.appointment.domain.model.AppointmentNote
import kotlinx.coroutines.flow.Flow

interface AppointmentNoteRepository {
  fun getNotesForAppointment(appointmentId: Int): Flow<List<AppointmentNote>>

  suspend fun addNote(appointmentId: Int, appointmentDate: Long, text: String)
  suspend fun updateNote(noteId: Int, newText: String)
  suspend fun deleteNote(noteId: Int)

  suspend fun clearUserData()
  suspend fun deleteExpiredNotes()
}