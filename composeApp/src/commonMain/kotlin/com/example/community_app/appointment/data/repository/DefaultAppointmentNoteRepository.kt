package com.example.community_app.appointment.data.repository

import com.example.community_app.appointment.data.local.appointment_note.AppointmentNoteDao
import com.example.community_app.appointment.data.local.appointment_note.AppointmentNoteEntity
import com.example.community_app.appointment.domain.model.AppointmentNote
import com.example.community_app.appointment.domain.repository.AppointmentNoteRepository
import com.example.community_app.core.util.addDays
import com.example.community_app.core.util.getCurrentTimeMillis
import com.example.community_app.profile.domain.repository.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

class DefaultAppointmentNoteRepository(
  private val dao: AppointmentNoteDao,
  private val userRepository: UserRepository
) : AppointmentNoteRepository {

  @OptIn(ExperimentalCoroutinesApi::class)
  override fun getNotesForAppointment(appointmentId: Int): Flow<List<AppointmentNote>> {
    return userRepository.getUser().flatMapLatest { user ->
      if (user != null) {
        dao.getNotes(appointmentId, user.id).map { entities ->
          entities.map { AppointmentNote(it.id, it.text, it.createdAt) }
        }
      } else {
        flowOf(emptyList())
      }
    }
  }

  override suspend fun addNote(appointmentId: Int, appointmentDate: Long, text: String) {
    val user = userRepository.getUser().firstOrNull() ?: return

    val entity = AppointmentNoteEntity(
      appointmentId = appointmentId,
      userId = user.id,
      text = text,
      createdAt = getCurrentTimeMillis(),
      appointmentDate = appointmentDate
    )
    dao.insertNote(entity)
  }

  override suspend fun updateNote(noteId: Int, newText: String) {
    dao.updateNoteText(noteId, newText)
  }

  override suspend fun deleteNote(noteId: Int) {
    dao.deleteNote(noteId)
  }

  override suspend fun clearUserData() {
    val user = userRepository.getUser().firstOrNull() ?: return
    dao.deleteAllNotesForUser(user.id)
  }

  override suspend fun deleteExpiredNotes() {
    val now = getCurrentTimeMillis()
    val timestamp = addDays(now, -7)

    dao.deleteExpiredNotes(timestamp)
  }
}