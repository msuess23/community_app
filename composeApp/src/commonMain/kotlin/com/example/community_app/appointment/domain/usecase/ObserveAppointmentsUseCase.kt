package com.example.community_app.appointment.domain.usecase

import com.example.community_app.appointment.domain.Appointment
import com.example.community_app.appointment.domain.AppointmentRepository
import com.example.community_app.auth.domain.AuthRepository
import com.example.community_app.auth.domain.AuthState
import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

class ObserveAppointmentsUseCase(
  private val appointmentRepository: AppointmentRepository,
  private val authRepository: AuthRepository
) {
  @OptIn(ExperimentalCoroutinesApi::class)
  operator fun invoke(): Flow<List<Appointment>> {
    return authRepository.authState.flatMapLatest { authState ->
      if (authState is AuthState.Authenticated) {
        appointmentRepository.getAppointments()
      } else {
        flowOf(emptyList())
      }
    }
  }

  suspend fun sync(): Result<Unit, DataError.Remote> {
    return appointmentRepository.refreshAppointments()
  }
}