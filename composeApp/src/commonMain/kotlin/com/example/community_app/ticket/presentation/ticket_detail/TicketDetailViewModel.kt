package com.example.community_app.ticket.presentation.ticket_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.community_app.app.navigation.Route
import com.example.community_app.auth.domain.AuthRepository
import com.example.community_app.auth.domain.getUserIdOrNull
import com.example.community_app.core.domain.Result
import com.example.community_app.media.domain.MediaRepository
import com.example.community_app.ticket.domain.TicketRepository
import com.example.community_app.util.BASE_URL
import com.example.community_app.util.MediaTargetType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TicketDetailViewModel(
  savedStateHandle: SavedStateHandle,
  private val ticketRepository: TicketRepository,
  private val mediaRepository: MediaRepository,
  private val authRepository: AuthRepository
) : ViewModel() {

  private val args = savedStateHandle.toRoute<Route.TicketDetail>()

  private val _state = MutableStateFlow(TicketDetailState())
  val state = _state
    .onStart {
      loadData()
    }
    .stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000L),
      TicketDetailState()
    )

  fun onAction(action: TicketDetailAction) {
    when(action) {
      TicketDetailAction.OnShowStatusHistory -> fetchStatusHistory()
      TicketDetailAction.OnDismissStatusHistory -> _state.update { it.copy(showStatusHistory = false) }
      else -> Unit
    }
  }

  private fun loadData() {
    if (args.isDraft) {
      loadDraft(args.id)
    } else {
      loadTicket(args.id.toInt())
    }
  }

  private fun loadDraft(id: Long) {
    viewModelScope.launch {
      val draft = ticketRepository.getDraft(id)
      _state.update { it.copy(
        draft = draft,
        isDraft = true,
        isOwner = true,
        imageUrls = draft?.images ?: emptyList()
      )}
    }
  }

  private fun loadTicket(id: Int) {
    ticketRepository.getTicket(id)
      .onEach { ticket ->
        if (ticket != null) {
          val userId = authRepository.getUserIdOrNull()
          val isOwner = userId != null && userId == ticket.creatorUserId

          _state.update { it.copy(
            ticket = ticket,
            isDraft = false,
            isOwner = isOwner
          ) }
          fetchImages(id)
        }
      }
      .launchIn(viewModelScope)

    viewModelScope.launch { ticketRepository.refreshTicket(id) }
  }

  private fun fetchImages(ticketId: Int) {
    viewModelScope.launch {
      val result = mediaRepository.getMediaList(MediaTargetType.TICKET, ticketId)
      if (result is Result.Success) {
        val urls = result.data.map { "$BASE_URL${it.url}" }
        _state.update { it.copy(imageUrls = urls) }
      }
    }
  }

  private fun fetchStatusHistory() {
    val id = _state.value.ticket?.id ?: return
    viewModelScope.launch {
      val result = ticketRepository.getStatusHistory(id)
      if (result is Result.Success) {
        _state.update { it.copy(statusHistory = result.data, showStatusHistory = true) }
      }
    }
  }
}