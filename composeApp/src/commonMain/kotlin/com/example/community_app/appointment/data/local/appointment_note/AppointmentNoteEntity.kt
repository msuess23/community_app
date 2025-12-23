package com.example.community_app.appointment.data.local.appointment_note

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
  tableName = "appointment_notes",
  indices = [Index("appointmentId"), Index("userId")]
)
data class AppointmentNoteEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Int = 0,
  val appointmentId: Int,
  val userId: Int,
  val text: String,
  val createdAt: Long,
  val appointmentDate: Long
)