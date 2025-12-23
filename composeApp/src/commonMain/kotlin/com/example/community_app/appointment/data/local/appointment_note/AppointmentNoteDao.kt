package com.example.community_app.appointment.data.local.appointment_note

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppointmentNoteDao {
  @Query("SELECT * FROM appointment_notes WHERE appointmentId = :appointmentId AND userId = :userId ORDER BY createdAt DESC")
  fun getNotes(appointmentId: Int, userId: Int): Flow<List<AppointmentNoteEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertNote(note: AppointmentNoteEntity)

  @Query("DELETE FROM appointment_notes WHERE id = :noteId")
  suspend fun deleteNote(noteId: Int)

  @Query("UPDATE appointment_notes SET text = :newText WHERE id = :noteId")
  suspend fun updateNoteText(noteId: Int, newText: String)

  @Query("DELETE FROM appointment_notes WHERE userId = :userId")
  suspend fun deleteAllNotesForUser(userId: Int)

  @Query("DELETE FROM appointment_notes WHERE appointmentDate < :timestamp")
  suspend fun deleteExpiredNotes(timestamp: Long)
}