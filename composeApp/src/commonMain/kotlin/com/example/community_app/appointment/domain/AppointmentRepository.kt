package com.example.community_app.appointment.domain

import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result

interface AppointmentRepository {
  suspend fun getFreeSlots(officeId: Int, from: String?, to: String?): Result<List<Slot>, DataError.Remote>

//   suspend fun getUserAppointments(): Result<List<Appointment>, DataError.Remote>
//   suspend fun bookSlot(officeId: Int, slotId: Int): Result<Appointment, DataError.Remote>
}