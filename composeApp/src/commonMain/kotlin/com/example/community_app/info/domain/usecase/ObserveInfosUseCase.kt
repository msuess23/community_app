package com.example.community_app.info.domain.usecase

import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.core.domain.location.Location
import com.example.community_app.core.domain.usecase.FetchUserLocationUseCase
import com.example.community_app.core.presentation.state.SyncStatus
import com.example.community_app.info.domain.Info
import com.example.community_app.info.domain.InfoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow

data class InfoDataResult(
  val infos: List<Info>,
  val userLocation: Location? = null,
  val error: DataError? = null,
  val isLoading: Boolean = false
)

class ObserveInfosUseCase(
  private val infoRepository: InfoRepository,
  private val fetchUserLocation: FetchUserLocationUseCase
) {
  operator fun invoke(forceRefresh: Boolean = false): Flow<InfoDataResult> {
    val syncFlow = flow {
      emit(InternalSyncState(status = SyncStatus(isLoading = true)))

      val location = fetchUserLocation().location

      val result = if (forceRefresh) {
        infoRepository.refreshInfos()
      } else if (location != null) {
        infoRepository.refreshInfos()
      } else {
        infoRepository.syncInfos()
      }

      val error = (result as? Result.Error)?.error
      emit(InternalSyncState(
        status = SyncStatus(isLoading = false, error = error),
        location = location
      ))
    }

    return combine(
      infoRepository.getInfos(),
      syncFlow
    ) { infos, syncState ->
      InfoDataResult(
        infos = infos,
        userLocation = syncState.location,
        error = syncState.status.error,
        isLoading = syncState.status.isLoading
      )
    }
  }

  private data class InternalSyncState(
    val status: SyncStatus,
    val location: Location? = null
  )
}