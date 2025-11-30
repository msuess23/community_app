package com.example.community_app.appointment.data.repository

import com.example.community_app.appointment.data.network.RemoteAppointmentDataSource
import com.example.community_app.appointment.domain.AppointmentRepository
import com.example.community_app.appointment.domain.Slot
import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.core.domain.map

class DefaultAppointmentRepository(
  private val remoteDataSource: RemoteAppointmentDataSource
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
}