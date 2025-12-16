package com.example.community_app.ticket.domain.usecase.master

import com.example.community_app.core.domain.Result
import com.example.community_app.core.presentation.state.SyncStatus
import com.example.community_app.ticket.domain.Ticket
import com.example.community_app.ticket.domain.TicketListItem
import com.example.community_app.ticket.domain.TicketRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow

data class TicketDataResult(
  val communityTickets: List<Ticket>,
  val myTickets: List<TicketListItem>,
  val syncStatus: SyncStatus
)

class ObserveTicketsUseCase(
  private val observeCommunityTickets: ObserveCommunityTicketsUseCase,
  private val observeMyTickets: ObserveMyTicketsUseCase,
  private val ticketRepository: TicketRepository
) {
  operator fun invoke(forceRefresh: Boolean): Flow<TicketDataResult> {
    val syncFlow = flow {
      emit(SyncStatus(isLoading = true))

      val result = ticketRepository.refreshTickets(force = forceRefresh)
      val error = (result as? Result.Error)?.error

      emit(SyncStatus(isLoading = false, error = error))
    }

    return combine(
      observeCommunityTickets(),
      observeMyTickets(),
      syncFlow
    ) { community, myTickets, syncState ->
      TicketDataResult(
        communityTickets = community,
        myTickets = myTickets,
        syncStatus = syncState
      )
    }
  }
}