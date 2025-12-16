package com.example.community_app.appointment.domain.usecase

import com.example.community_app.appointment.domain.AppointmentRepository
import com.example.community_app.appointment.domain.Slot
import com.example.community_app.core.domain.Result
import com.example.community_app.core.presentation.state.SyncStatus
import com.example.community_app.core.util.addDays
import com.example.community_app.core.util.getCurrentTimeMillis
import com.example.community_app.core.util.toIso8601
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

data class SlotsResult(
  val slots: List<Slot>,
  val syncStatus: SyncStatus
)

class GetFreeSlotsUseCase(
  private val repository: AppointmentRepository
) {
  private val DAYS_IN_FUTURE = 90

  operator fun invoke(officeId: Int): Flow<SlotsResult> = flow {
    emit(SlotsResult(emptyList(), SyncStatus(isLoading = true)))

    val nowMillis = getCurrentTimeMillis()
    val endDateMillis = addDays(nowMillis, DAYS_IN_FUTURE)

    val from = toIso8601(nowMillis)
    val to = toIso8601(endDateMillis)

    when(val result = repository.getFreeSlots(officeId, from, to)) {
      is Result.Success -> {
        emit(SlotsResult(
          slots = result.data,
          syncStatus = SyncStatus(isLoading = false)
        ))
      }
      is Result.Error -> {
        emit(SlotsResult(
          slots = emptyList(),
          syncStatus = SyncStatus(isLoading = false, error = result.error)
        ))
      }
    }
  }
}