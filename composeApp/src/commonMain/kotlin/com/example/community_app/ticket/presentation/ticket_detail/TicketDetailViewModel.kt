package com.example.community_app.ticket.presentation.ticket_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.community_app.app.navigation.Route
import com.example.community_app.core.data.local.favorite.FavoriteType
import com.example.community_app.core.domain.usecase.ToggleFavoriteUseCase
import com.example.community_app.core.presentation.helpers.toUiText
import com.example.community_app.ticket.domain.usecase.detail.ObserveTicketDetailUseCase
import com.example.community_app.ticket.domain.usecase.detail.VoteTicketUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TicketDetailViewModel(
  savedStateHandle: SavedStateHandle,
  private val observeTicketDetail: ObserveTicketDetailUseCase,
  private val toggleFavorite: ToggleFavoriteUseCase,
  private val voteTicket: VoteTicketUseCase
) : ViewModel() {
  private val args = savedStateHandle.toRoute<Route.TicketDetail>()

  private val _showStatusHistory = MutableStateFlow(false)

  private val detailFlow = observeTicketDetail(args.id, args.isDraft)

  val state = combine(
    detailFlow,
    _showStatusHistory
  ) { result, showHistory ->
    val fallbackImage = result.ticket?.imageUrl
    val finalImages = result.images.ifEmpty { listOfNotNull(fallbackImage) }

    TicketDetailState(
      isLoading = result.syncStatus.isLoading,
      ticket = result.ticket,
      draft = result.draft,
      isDraft = args.isDraft,
      isOwner = result.isOwner,
      imageUrls = finalImages,
      showStatusHistory = showHistory,
      statusHistory = result.history,
      errorMessage = result.syncStatus.error?.toUiText()
    )
  }.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5000L),
    TicketDetailState(isLoading = true)
  )

  fun onAction(action: TicketDetailAction) {
    when(action) {
      TicketDetailAction.OnShowStatusHistory -> _showStatusHistory.value = true
      TicketDetailAction.OnDismissStatusHistory -> _showStatusHistory.value = false
      TicketDetailAction.OnToggleFavorite -> toggleFavorite()
      TicketDetailAction.OnVote -> toggleVote()
      else -> Unit
    }
  }

  private fun toggleFavorite() {
    val currentTicket = state.value.ticket ?: return
    if (state.value.isDraft || state.value.isOwner) return

    viewModelScope.launch {
      toggleFavorite(
        itemId = currentTicket.id,
        type = FavoriteType.TICKET,
        isFavorite = !currentTicket.isFavorite
      )
    }
  }

  private fun toggleVote() {
    val currentTicket = state.value.ticket ?: return
    viewModelScope.launch {
      voteTicket(currentTicket.id, currentTicket.userVoted ?: false)
    }
  }
}