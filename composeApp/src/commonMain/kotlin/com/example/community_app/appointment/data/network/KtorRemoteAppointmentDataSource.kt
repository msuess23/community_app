package com.example.community_app.appointment.data.network

import com.example.community_app.core.data.safeCall
import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.dto.AppointmentDto
import com.example.community_app.dto.SlotDto
import com.example.community_app.util.BASE_URL
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post

class KtorRemoteAppointmentDataSource(
  private val httpClient: HttpClient
) : RemoteAppointmentDataSource {

  override suspend fun getFreeSlots(
    officeId: Int,
    from: String?,
    to: String?
  ): Result<List<SlotDto>, DataError.Remote> {
    return safeCall {
      httpClient.get("$BASE_URL/api/office/$officeId/slot") {
        if (from != null) parameter("from", from)
        if (to != null) parameter("to", to)
      }
    }
  }

  override suspend fun bookSlot(officeId: Int, slotId: Int): Result<AppointmentDto, DataError.Remote> {
    return safeCall {
      httpClient.post("$BASE_URL/api/office/$officeId/slot/$slotId")
    }
  }
}