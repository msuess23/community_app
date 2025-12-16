package com.example.community_app.info.domain.usecase

import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.core.presentation.state.SyncStatus
import com.example.community_app.dto.InfoStatusDto
import com.example.community_app.info.domain.Info
import com.example.community_app.info.domain.InfoRepository
import com.example.community_app.media.domain.usecase.GetImagesUseCase
import com.example.community_app.util.InfoStatus
import com.example.community_app.util.MediaTargetType
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow

data class InfoDetailResult(
  val info: Info?,
  val imageUrls: List<String> = emptyList(),
  val statusHistory: List<InfoStatusDto> = emptyList(),
  val syncStatus: SyncStatus
)

class GetInfoDetailUseCase(
  private val infoRepository: InfoRepository,
  private val getImages: GetImagesUseCase
) {
  operator fun invoke(infoId: Int): Flow<InfoDetailResult> {
    val loadFlow = flow {
      emit(LoadState(syncStatus = SyncStatus(isLoading = true)))

      coroutineScope {
        val imagesDeferred = async {
          getImages(MediaTargetType.INFO, infoId)
        }

        val historyDeferred = async {
          infoRepository.getStatusHistory(infoId)
        }

        val refreshDeferred = async {
          infoRepository.refreshInfo(infoId)
        }

        val imagesResult = imagesDeferred.await()
        val historyResult = historyDeferred.await()
        val refreshResult = refreshDeferred.await()

        val images = (imagesResult as? Result.Success)?.data ?: emptyList()
        val history = (historyResult as? Result.Success)?.data ?: emptyList()

        val error = (refreshResult as? Result.Error)?.error
          ?: (historyResult as? Result.Error)?.error
          ?: (imagesResult as? Result.Error)?.error

        emit(LoadState(
          images = images,
          history = history,
          syncStatus = SyncStatus(
            isLoading = false,
            error = error
          )
        ))
      }
    }

    return combine(
      infoRepository.getInfo(infoId),
      loadFlow
    ) { info, loadState ->
      InfoDetailResult(
        info = info,
        imageUrls = loadState.images,
        statusHistory = loadState.history,
        syncStatus = loadState.syncStatus
      )
    }
  }

  private data class LoadState(
    val images: List<String> = emptyList(),
    val history: List<InfoStatusDto> = emptyList(),
    val syncStatus: SyncStatus
  )
}