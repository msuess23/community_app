package com.example.community_app.info.presentation.info_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.community_app.app.navigation.Route
import com.example.community_app.core.domain.Result
import com.example.community_app.info.domain.InfoRepository
import com.example.community_app.media.domain.MediaRepository
import com.example.community_app.util.BASE_URL
import com.example.community_app.util.MediaTargetType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InfoDetailViewModel(
  savedStateHandle: SavedStateHandle,
  private val infoRepository: InfoRepository,
  private val mediaRepository: MediaRepository
) : ViewModel() {
  private val infoId = savedStateHandle.toRoute<Route.InfoDetail>().id

  private val _showStatusHistory = MutableStateFlow(false)
  private val _isLoading = MutableStateFlow(false)

  private val infoFlow = infoRepository.getInfo(infoId)
  private val additionalDataFlow = flow {
    _isLoading.value = true
    val imagesResult = mediaRepository.getMediaList(
      targetType = MediaTargetType.INFO,
      targetId = infoId
    )

    val historyResult = infoRepository.getStatusHistory(infoId)

    val imageUrls = if (imagesResult is Result.Success) {
      imagesResult.data.map { "$BASE_URL${it.url}" }
    } else emptyList()

    val history = if (historyResult is Result.Success) {
      historyResult.data
    } else emptyList()

    emit(Pair(imageUrls, history))
    _isLoading.value = false
  }

  val state = combine(
    infoFlow,
    additionalDataFlow,
    _showStatusHistory,
    _isLoading
  ) { info, (images, history), showHistory, loading ->
    val finalImages = images.ifEmpty { listOfNotNull(info?.imageUrl) }

    InfoDetailState(
      isLoading = loading,
      info = info,
      imageUrls = finalImages,
      showStatusHistory = showHistory,
      statusHistory = history
    )
  }.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5000L),
    InfoDetailState(isLoading = true)
  )

  init {
    refreshInfoData()
  }

  fun onAction(action: InfoDetailAction) {
    when (action) {
      InfoDetailAction.OnShowStatusHistory -> _showStatusHistory.value = true
      InfoDetailAction.OnDismissStatusHistory -> _showStatusHistory.value = false
      else -> Unit
    }
  }

  private fun refreshInfoData() {
    viewModelScope.launch {
      infoRepository.refreshInfo(infoId)
    }
  }
}