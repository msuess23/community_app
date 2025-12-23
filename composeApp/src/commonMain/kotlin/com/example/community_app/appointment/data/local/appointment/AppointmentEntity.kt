package com.example.community_app.appointment.data.local.appointment

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "appointments")
data class AppointmentEntity(
  @PrimaryKey(autoGenerate = false)
  val id: Int,
  val officeId: Int,
  val userId: Int,
  val startsAt: String,
  val endsAt: String,
  val calendarEventId: String?
)