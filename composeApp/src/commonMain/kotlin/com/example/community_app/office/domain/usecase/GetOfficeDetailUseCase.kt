package com.example.community_app.office.domain.usecase

import com.example.community_app.appointment.domain.Slot
import com.example.community_app.appointment.domain.usecase.detail.GetFreeSlotsUseCase
import com.example.community_app.core.domain.Result
import com.example.community_app.core.presentation.state.SyncStatus
import com.example.community_app.office.domain.Office
import com.example.community_app.office.domain.OfficeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow

data class OfficeDetailResult(
  val office: Office?,
  val officeSyncStatus: SyncStatus,
  val allSlots: List<Slot> = emptyList(),
  val slotsSyncStatus: SyncStatus
)

class GetOfficeDetailUseCase(
  private val officeRepository: OfficeRepository,
  private val getFreeSlots: GetFreeSlotsUseCase
) {
  operator fun invoke(officeId: Int): Flow<OfficeDetailResult> {
    val officeLoadFlow = flow {
      emit(SyncStatus(isLoading = true))
      val result = officeRepository.refreshOffice(officeId)
      val error = (result as? Result.Error)?.error
      emit(SyncStatus(isLoading = false, error = error))
    }

    val slotsLoadFlow = getFreeSlots(officeId)

    return combine(
      officeRepository.getOffice(officeId),
      officeLoadFlow,
      slotsLoadFlow
    ) { office, officeSync, slotsResult ->
      OfficeDetailResult(
        office = office,
        officeSyncStatus = officeSync,
        allSlots = slotsResult.slots,
        slotsSyncStatus = slotsResult.syncStatus
      )
    }
  }
}

