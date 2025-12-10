package com.example.community_app.appointment.domain.usecase

import com.example.community_app.appointment.domain.AppointmentRepository
import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result

class CancelAppointmentUseCase(
  private val repository: AppointmentRepository
) {
  suspend operator fun invoke(id: Int): Result<Unit, DataError.Remote> {
    return repository.cancelAppointment(id)
  }
}