package com.example.community_app.appointment.domain.usecase

import com.example.community_app.appointment.domain.Appointment
import com.example.community_app.appointment.domain.AppointmentRepository
import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.office.domain.Office
import com.example.community_app.office.domain.OfficeRepository
import kotlinx.coroutines.flow.first

data class AppointmentDetails(
  val appointment: Appointment,
  val office: Office?
)

class GetAppointmentDetailsUseCase(
  private val appointmentRepository: AppointmentRepository,
  private val officeRepository: OfficeRepository
) {
  suspend operator fun invoke(id: Int): Result<AppointmentDetails, DataError> {
    val appointment = appointmentRepository.getAppointment(id).first()
      ?: return Result.Error(DataError.Local.UNKNOWN)

    var office = officeRepository.getOffice(appointment.officeId).first()

    if (office == null) {
      val refreshResult = officeRepository.refreshOffice(appointment.officeId)
      if (refreshResult is Result.Success) {
        office = officeRepository.getOffice(appointment.officeId).first()
      }
    }

    return Result.Success(AppointmentDetails(appointment, office))
  }
}