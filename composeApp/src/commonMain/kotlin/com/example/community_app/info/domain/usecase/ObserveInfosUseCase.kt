package com.example.community_app.info.domain.usecase

import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.core.presentation.state.SyncStatus
import com.example.community_app.info.domain.model.Info
import com.example.community_app.info.domain.repository.InfoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow

data class InfoDataResult(
  val infos: List<Info>,
  val error: DataError? = null,
  val isLoading: Boolean = false
)

class ObserveInfosUseCase(
  private val infoRepository: InfoRepository
) {
  operator fun invoke(forceRefresh: Boolean = false): Flow<InfoDataResult> {
    val syncFlow = flow {
      emit(SyncStatus(isLoading = true))

      val result = infoRepository.refreshInfos(force = forceRefresh)
      val error = (result as? Result.Error)?.error

      emit(SyncStatus(isLoading = false, error = error))
    }

    return combine(
      infoRepository.getInfos(),
      syncFlow
    ) { infos, syncState ->
      InfoDataResult(
        infos = infos,
        error = syncState.error,
        isLoading = syncState.isLoading
      )
    }
  }
}