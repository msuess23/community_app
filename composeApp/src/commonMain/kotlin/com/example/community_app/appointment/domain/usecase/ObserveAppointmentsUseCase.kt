package com.example.community_app.appointment.domain.usecase

import com.example.community_app.appointment.domain.Appointment
import com.example.community_app.appointment.domain.AppointmentRepository
import com.example.community_app.auth.domain.AuthRepository
import com.example.community_app.auth.domain.AuthState
import com.example.community_app.core.domain.Result
import com.example.community_app.core.presentation.state.SyncStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

data class AppointmentDataResult(
  val appointments: List<Appointment>,
  val syncStatus: SyncStatus
)

class ObserveAppointmentsUseCase(
  private val appointmentRepository: AppointmentRepository,
  private val authRepository: AuthRepository
) {
  @OptIn(ExperimentalCoroutinesApi::class)
  operator fun invoke(forceRefresh: Boolean): Flow<AppointmentDataResult> {
    return authRepository.authState.flatMapLatest { authState ->
      if (authState is AuthState.Authenticated) {
        val syncFlow = flow {
          emit(SyncStatus(isLoading = true))

          val result = if (forceRefresh) {
            appointmentRepository.refreshAppointments()
          } else {
            appointmentRepository.syncAppointments()
          }

          val error = (result as? Result.Error)?.error
          emit(SyncStatus(isLoading = false, error = error))
        }

        combine(
          appointmentRepository.getAppointments(),
          syncFlow
        ) { appointments, status ->
          AppointmentDataResult(
            appointments = appointments,
            syncStatus = status
          )
        }
      } else {
        flowOf(
          AppointmentDataResult(
            appointments = emptyList(),
            syncStatus = SyncStatus(isLoading = false)
          )
        )
      }
    }
  }
}