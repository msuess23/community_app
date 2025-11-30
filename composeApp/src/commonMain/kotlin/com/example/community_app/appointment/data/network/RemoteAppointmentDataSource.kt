package com.example.community_app.appointment.data.network

import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.dto.AppointmentDto
import com.example.community_app.dto.SlotDto

interface RemoteAppointmentDataSource {
  suspend fun getFreeSlots(officeId: Int, from: String?, to: String?): Result<List<SlotDto>, DataError.Remote>
  suspend fun bookSlot(officeId: Int, slotId: Int): Result<AppointmentDto, DataError.Remote>
}