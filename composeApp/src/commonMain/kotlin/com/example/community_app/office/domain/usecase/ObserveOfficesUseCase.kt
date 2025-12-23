package com.example.community_app.office.domain.usecase

import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.core.presentation.state.SyncStatus
import com.example.community_app.office.domain.model.Office
import com.example.community_app.office.domain.repository.OfficeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow

data class OfficeDataResult(
  val offices: List<Office>,
  val error: DataError? = null,
  val isLoading: Boolean = false
)

class ObserveOfficesUseCase(
  private val officeRepository: OfficeRepository
) {
  operator fun invoke(forceRefresh: Boolean = false): Flow<OfficeDataResult> {
    val syncFlow = flow {
      emit(SyncStatus(isLoading = true))

      val result = officeRepository.refreshOffices(force = forceRefresh)
      val error = (result as? Result.Error)?.error

      emit(SyncStatus(isLoading = false, error = error))
    }

    return combine(
      officeRepository.getOffices(),
      syncFlow
    ) { offices, syncState ->
      OfficeDataResult(
        offices = offices,
        error = syncState.error,
        isLoading = syncState.isLoading
      )
    }
  }
}