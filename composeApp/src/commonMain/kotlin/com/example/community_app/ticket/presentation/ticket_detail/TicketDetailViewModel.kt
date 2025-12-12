package com.example.community_app.ticket.presentation.ticket_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.community_app.app.navigation.Route
import com.example.community_app.auth.domain.AuthRepository
import com.example.community_app.auth.domain.getUserIdOrNull
import com.example.community_app.core.data.local.favorite.FavoriteType
import com.example.community_app.core.domain.Result
import com.example.community_app.core.domain.usecase.ToggleFavoriteUseCase
import com.example.community_app.dto.TicketStatusDto
import com.example.community_app.ticket.domain.Ticket
import com.example.community_app.ticket.domain.TicketDraft
import com.example.community_app.ticket.domain.TicketRepository
import com.example.community_app.ticket.domain.usecase.detail.SyncTicketImagesUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TicketDetailViewModel(
  savedStateHandle: SavedStateHandle,
  private val ticketRepository: TicketRepository,
  private val authRepository: AuthRepository,
  private val syncTicketImages: SyncTicketImagesUseCase,
  private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {
  private val args = savedStateHandle.toRoute<Route.TicketDetail>()

  private val _showStatusHistory = MutableStateFlow(false)
  private val _isLoading = MutableStateFlow(false)

  private val ticketDataFlow = flow {
    if (args.isDraft) {
      val draft = ticketRepository.getDraft(args.id)
      emit(TicketDetailData.Draft(draft))
    } else {
      ticketRepository.getTicket(args.id.toInt()).collect { ticket ->
        emit(TicketDetailData.Remote(ticket))
      }
    }
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  private val additionalDataFlow = ticketDataFlow.mapLatest { data ->
    when (data) {
      is TicketDetailData.Draft -> {
        AdditionalData(
          images = data.draft?.images ?: emptyList(),
          history = emptyList(),
          isOwner = true
        )
      }
      is TicketDetailData.Remote -> {
        val ticket = data.ticket ?: return@mapLatest AdditionalData()
        val userId = authRepository.getUserIdOrNull()
        val isOwner = userId != null && userId == ticket.creatorUserId

        val imagesResult = syncTicketImages(ticket.id, isOwner)
        val historyResult = ticketRepository.getStatusHistory(ticket.id)

        val images = if (imagesResult is Result.Success) imagesResult.data else emptyList()
        val history = if (historyResult is Result.Success) historyResult.data else emptyList()

        AdditionalData(images, history, isOwner)
      }
    }
  }

  val state = combine(
    ticketDataFlow,
    additionalDataFlow,
    _showStatusHistory,
    _isLoading
  ) { data, additional, showHistory, loading ->
    val (ticket, draft) = when (data) {
      is TicketDetailData.Remote -> data.ticket to null
      is TicketDetailData.Draft -> null to data.draft
    }

    val fallbackImage = ticket?.imageUrl
    val finalImages = additional.images.ifEmpty { listOfNotNull(fallbackImage) }

    TicketDetailState(
      isLoading = loading,
      ticket = ticket,
      draft = draft,
      isDraft = args.isDraft,
      isOwner = additional.isOwner,
      imageUrls = finalImages,
      showStatusHistory = showHistory,
      statusHistory = additional.history
    )
  }.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5000L),
    TicketDetailState(isLoading = true)
  )

  init {
    if (!args.isDraft) {
      refreshTicket()
    }
  }

  fun onAction(action: TicketDetailAction) {
    when(action) {
      TicketDetailAction.OnShowStatusHistory -> _showStatusHistory.value = true
      TicketDetailAction.OnDismissStatusHistory -> _showStatusHistory.value = false
      TicketDetailAction.OnToggleFavorite -> toggleFavorite()
      else -> Unit
    }
  }

  private fun refreshTicket() {
    viewModelScope.launch {
      _isLoading.update { true }
      ticketRepository.refreshTicket(args.id.toInt())
      _isLoading.update { false }
    }
  }

  private fun toggleFavorite() {
    val currentTicket = state.value.ticket ?: return
    if (state.value.isDraft || state.value.isOwner) return

    viewModelScope.launch {
      toggleFavoriteUseCase(
        itemId = currentTicket.id,
        type = FavoriteType.TICKET,
        isFavorite = !currentTicket.isFavorite
      )
    }
  }

  sealed interface TicketDetailData {
    data class Remote(val ticket: Ticket?) : TicketDetailData
    data class Draft(val draft: TicketDraft?) : TicketDetailData
  }

  data class AdditionalData(
    val images: List<String> = emptyList(),
    val history: List<TicketStatusDto> = emptyList(),
    val isOwner: Boolean = false
  )
}