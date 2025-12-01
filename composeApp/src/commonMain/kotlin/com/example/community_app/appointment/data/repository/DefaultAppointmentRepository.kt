package com.example.community_app.appointment.data.repository

import com.example.community_app.appointment.data.local.AppointmentDao
import com.example.community_app.appointment.data.mappers.toAppointment
import com.example.community_app.appointment.data.mappers.toEntity
import com.example.community_app.appointment.data.network.RemoteAppointmentDataSource
import com.example.community_app.appointment.domain.Appointment
import com.example.community_app.appointment.domain.AppointmentRepository
import com.example.community_app.appointment.domain.Slot
import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.core.domain.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DefaultAppointmentRepository(
  private val remoteDataSource: RemoteAppointmentDataSource,
  private val appointmentDao: AppointmentDao
) : AppointmentRepository {

  override suspend fun getFreeSlots(
    officeId: Int,
    from: String?,
    to: String?
  ): Result<List<Slot>, DataError.Remote> {
    return remoteDataSource.getFreeSlots(officeId, from, to).map { dtos ->
      dtos.map { Slot(it.id, it.startsAt, it.endsAt) }
    }
  }

  override suspend fun bookSlot(
    officeId: Int,
    slotId: Int
  ): Result<Appointment, DataError.Remote> {
    val result = remoteDataSource.bookSlot(officeId, slotId)

    if (result is Result.Success) {
      appointmentDao.upsertAppointments(listOf(result.data.toEntity()))
    }

    return result.map { it.toEntity().toAppointment() }
  }

  override fun getAppointments(): Flow<List<Appointment>> {
    return appointmentDao.getAppointments().map { list -> list.map { it.toAppointment() } }
  }

  override fun getAppointment(id: Int): Flow<Appointment?> {
    return appointmentDao.getAppointmentById(id).map { it?.toAppointment() }
  }

  override suspend fun refreshAppointments(): Result<Unit, DataError.Remote> {
    return when(val result = remoteDataSource.getUserAppointments()) {
      is Result.Success -> {
        appointmentDao.replaceAll(result.data.map { it.toEntity() })
        Result.Success(Unit)
      }
      is Result.Error -> Result.Error(result.error)
    }
  }

  override suspend fun cancelAppointment(id: Int): Result<Unit, DataError.Remote> {
    val result = remoteDataSource.cancelAppointment(id)
    if (result is Result.Success) {
      appointmentDao.deleteAppointment(id)
    }
    return result
  }
}