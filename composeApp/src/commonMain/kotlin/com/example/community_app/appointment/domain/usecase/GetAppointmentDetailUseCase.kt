package com.example.community_app.appointment.domain.usecase

import com.example.community_app.appointment.domain.Appointment
import com.example.community_app.appointment.domain.AppointmentRepository
import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.office.domain.Office
import com.example.community_app.office.domain.OfficeRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transformLatest

data class AppointmentDetails(
  val appointment: Appointment,
  val office: Office?
)

class GetAppointmentDetailsUseCase(
  private val appointmentRepository: AppointmentRepository,
  private val officeRepository: OfficeRepository
) {
  @OptIn(ExperimentalCoroutinesApi::class)
  operator fun invoke(id: Int): Flow<Result<AppointmentDetails, DataError>> {
    return appointmentRepository.getAppointment(id).flatMapLatest { appointment ->
      if (appointment == null) {
        flowOf(Result.Error(DataError.Local.UNKNOWN) as Result<AppointmentDetails, DataError>)
      } else {
        officeRepository.getOffice(appointment.officeId).transformLatest { office ->
          if (office != null) {
            emit(Result.Success(AppointmentDetails(appointment, office)))
          } else {
            val result = officeRepository.refreshOffice(appointment.officeId)

            if (result is Result.Error) {
              emit(Result.Error(result.error) as Result<AppointmentDetails, DataError>)
            }
          }
        }
      }
    }
  }
}