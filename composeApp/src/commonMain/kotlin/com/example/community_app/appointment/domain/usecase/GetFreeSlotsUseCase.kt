package com.example.community_app.appointment.domain.usecase

import com.example.community_app.appointment.domain.AppointmentRepository
import com.example.community_app.appointment.domain.Slot
import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result

class GetFreeSlotsUseCase(
  private val repository: AppointmentRepository
) {
  suspend operator fun invoke(officeId: Int, from: String, to: String): Result<List<Slot>, DataError.Remote> {
    return repository.getFreeSlots(officeId, from = from, to = to)
  }
}