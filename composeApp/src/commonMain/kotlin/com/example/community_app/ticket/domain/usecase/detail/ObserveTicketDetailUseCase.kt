package com.example.community_app.ticket.domain.usecase.detail

import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.core.presentation.state.SyncStatus
import com.example.community_app.profile.domain.repository.UserRepository
import com.example.community_app.profile.domain.repository.getUserIdOrNull
import com.example.community_app.ticket.domain.model.Ticket
import com.example.community_app.ticket.domain.model.TicketDraft
import com.example.community_app.ticket.domain.repository.TicketRepository
import com.example.community_app.ticket.domain.model.TicketStatusEntry
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

data class TicketDetailResult(
  val ticket: Ticket? = null,
  val draft: TicketDraft? = null,
  val images: List<String> = emptyList(),
  val history: List<TicketStatusEntry> = emptyList(),
  val isOwner: Boolean = false,
  val syncStatus: SyncStatus
)

class ObserveTicketDetailUseCase(
  private val ticketRepository: TicketRepository,
  private val userRepository: UserRepository,
  private val syncTicketImages: SyncTicketImagesUseCase
) {
  operator fun invoke(id: Long, isDraft: Boolean): Flow<TicketDetailResult> {
    return if (isDraft) {
      observeDraft(id)
    } else {
      observeTicket(id.toInt())
    }
  }

  private fun observeDraft(draftId: Long): Flow<TicketDetailResult> = flow {
    val draft = ticketRepository.getDraft(draftId)
    emit(
      TicketDetailResult(
        draft = draft,
        images = draft?.images ?: emptyList(),
        isOwner = true,
        syncStatus = SyncStatus(isLoading = false)
      )
    )
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  private fun observeTicket(ticketId: Int): Flow<TicketDetailResult> {
    val ticketFlow = ticketRepository.getTicket(ticketId)

    val loadFlow = flow {
      emit(LoadState(isLoading = true))

      coroutineScope {
        val historyDeferred = async { ticketRepository.getStatusHistory(ticketId) }
        val refreshedDeferred = async { ticketRepository.refreshTicket(ticketId) }

        val imagesDeferred = async {
          val userId = userRepository.getUserIdOrNull()
          var localTicket = ticketRepository.getTicket(ticketId).first()

          if (localTicket == null) {
            refreshedDeferred.await()
            localTicket = ticketRepository.getTicket(ticketId).first()
          }

          val isOwner = userId != null && localTicket?.creatorUserId == userId

          syncTicketImages(ticketId, isOwner)
        }

        val historyResult = historyDeferred.await()
        val refreshResult = refreshedDeferred.await()
        val imagesResult = imagesDeferred.await()

        val error = (refreshResult as? Result.Error)?.error
          ?: (historyResult as? Result.Error)?.error
          ?: (imagesResult as? Result.Error)?.error

        emit(LoadState(
          isLoading = false,
          history = (historyResult as? Result.Success)?.data ?: emptyList(),
          images = (imagesResult as? Result.Success)?.data ?: emptyList(),
          error = error
        ))
      }


    }

    return combine(
      ticketFlow,
      loadFlow
    ) { ticket, loadState ->
      val userId = userRepository.getUserIdOrNull()
      val isOwner = userId != null && ticket?.creatorUserId == userId

      TicketDetailResult(
        ticket = ticket,
        isOwner = isOwner,
        images = loadState.images,
        history = loadState.history,
        syncStatus = SyncStatus(
          isLoading = loadState.isLoading,
          error = loadState.error
        )
      )
    }
  }

  private data class LoadState(
    val isLoading: Boolean,
    val history: List<TicketStatusEntry> = emptyList(),
    val images: List<String> = emptyList(),
    val error: DataError? = null
  )
}