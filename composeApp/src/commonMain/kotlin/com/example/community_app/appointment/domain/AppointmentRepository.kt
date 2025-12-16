package com.example.community_app.appointment.domain

import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import kotlinx.coroutines.flow.Flow

interface AppointmentRepository {
  suspend fun getFreeSlots(officeId: Int, from: String?, to: String?): Result<List<Slot>, DataError.Remote>
  suspend fun bookSlot(officeId: Int, slotId: Int): Result<Appointment, DataError.Remote>

  suspend fun updateCalendarEventId(appointmentId: Int, eventId: String?)

  fun getAppointments(): Flow<List<Appointment>>
  fun getAppointment(id: Int): Flow<Appointment?>
  suspend fun refreshAppointments(force: Boolean = false): Result<Unit, DataError.Remote>
  suspend fun cancelAppointment(id: Int): Result<Unit, DataError.Remote>
}