package com.example.community_app.appointment.domain.usecase

import com.example.community_app.appointment.domain.AppointmentRepository
import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.core.domain.map
import com.example.community_app.settings.domain.SettingsRepository
import kotlinx.coroutines.flow.first

class BookAppointmentUseCase(
  private val repository: AppointmentRepository,
  private val settingsRepository: SettingsRepository,
  private val exportAppointmentToCalendarUseCase: ExportAppointmentToCalendarUseCase
) {
  suspend operator fun invoke(officeId: Int, slotId: Int): Result<Unit, DataError.Remote> {
    val result = repository.bookSlot(officeId, slotId)

    if (result is Result.Success) {
      val settings = settingsRepository.settings.first()
      if (settings.calendarSyncEnabled) {
        exportAppointmentToCalendarUseCase(result.data)
      }
      return Result.Success(Unit)
    }
    return result.map { }
  }
}