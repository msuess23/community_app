package com.example.community_app.appointment.data.repository

import com.example.community_app.appointment.data.local.appointment.AppointmentDao
import com.example.community_app.appointment.data.mappers.toAppointment
import com.example.community_app.appointment.data.mappers.toEntity
import com.example.community_app.appointment.data.network.RemoteAppointmentDataSource
import com.example.community_app.appointment.domain.model.Appointment
import com.example.community_app.appointment.domain.repository.AppointmentRepository
import com.example.community_app.appointment.domain.model.Slot
import com.example.community_app.core.data.sync.SyncManager
import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.core.domain.map
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class DefaultAppointmentRepository(
  private val remoteDataSource: RemoteAppointmentDataSource,
  private val appointmentDao: AppointmentDao,
  private val syncManager: SyncManager
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

  override suspend fun updateCalendarEventId(appointmentId: Int, eventId: String?) {
    val entity = appointmentDao.getAppointmentById(appointmentId).first()
    if (entity != null) {
      appointmentDao.upsertAppointments(
        listOf(entity.copy(calendarEventId = eventId))
      )
    }
  }

  override fun getAppointments(): Flow<List<Appointment>> {
    return appointmentDao.getAppointments().map { list -> list.map { it.toAppointment() } }
  }

  override fun getAppointment(id: Int): Flow<Appointment?> {
    return appointmentDao.getAppointmentById(id).map { it?.toAppointment() }
  }

  override suspend fun refreshAppointments(force: Boolean): Result<Unit, DataError.Remote> = coroutineScope{
    val decision = syncManager.checkSyncStatus(
      featureKey = "appointment",
      forceRefresh = force
    )

    if (!decision.shouldFetch) {
      return@coroutineScope Result.Success(Unit)
    }

    when (val result = remoteDataSource.getUserAppointments()) {
      is Result.Success -> {
        val localAppointments = appointmentDao.getAppointments().first()
        val localIdMap = localAppointments.associate { it.id to it.calendarEventId }

        val newAppointments = result.data.map { dto ->
          val existingCalendarId = localIdMap[dto.id]
          dto.toEntity(calendarEventId = existingCalendarId)
        }

        appointmentDao.replaceAll(newAppointments)
        syncManager.updateSyncSuccess("appointment", decision.currentLocation)
        Result.Success(Unit)
      }
      is Result.Error -> {
        Result.Error(result.error)
      }
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