package com.example.community_app.info.presentation.info_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.community_app.app.navigation.Route
import com.example.community_app.core.presentation.helpers.toUiText
import com.example.community_app.info.domain.usecase.GetInfoDetailUseCase
import com.example.community_app.info.domain.usecase.ToggleInfoFavoriteUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InfoDetailViewModel(
  savedStateHandle: SavedStateHandle,
  private val getInfoDetail: GetInfoDetailUseCase,
  private val toggleFavoriteUseCase: ToggleInfoFavoriteUseCase
) : ViewModel() {
  private val infoId = savedStateHandle.toRoute<Route.InfoDetail>().id

  private val _showStatusHistory = MutableStateFlow(false)
  private val _descriptionExpanded = MutableStateFlow(false)

  private val infoFlow = getInfoDetail(infoId)

  val state = combine(
    infoFlow,
    _showStatusHistory,
    _descriptionExpanded
  ) { result, showHistory, expanded ->
    val finalImages = result.imageUrls.ifEmpty {
      listOfNotNull(result.info?.imageUrl)
    }

    InfoDetailState(
      isLoading = result.syncStatus.isLoading,
      info = result.info,
      imageUrls = finalImages,
      showStatusHistory = showHistory,
      statusHistory = result.statusHistory,
      errorMessage = result.syncStatus.error?.toUiText(),
      isDescriptionExpanded = expanded
    )
  }.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5000L),
    InfoDetailState(isLoading = true)
  )

  fun onAction(action: InfoDetailAction) {
    when (action) {
      InfoDetailAction.OnShowStatusHistory -> _showStatusHistory.value = true
      InfoDetailAction.OnDismissStatusHistory -> _showStatusHistory.value = false
      InfoDetailAction.OnToggleFavorite -> toggleFavorite()
      InfoDetailAction.OnToggleDescription -> _descriptionExpanded.value = !_descriptionExpanded.value
      else -> Unit
    }
  }

  private fun toggleFavorite() {
    val currentInfo = state.value.info ?: return

    viewModelScope.launch {
      toggleFavoriteUseCase(
        itemId = currentInfo.id,
        isFavorite = !currentInfo.isFavorite
      )
    }
  }
}