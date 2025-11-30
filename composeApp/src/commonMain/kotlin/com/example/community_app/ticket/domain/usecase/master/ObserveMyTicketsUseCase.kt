package com.example.community_app.ticket.domain.usecase.master

import com.example.community_app.auth.domain.AuthRepository
import com.example.community_app.auth.domain.AuthState
import com.example.community_app.ticket.domain.TicketListItem
import com.example.community_app.ticket.domain.TicketRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

class ObserveMyTicketsUseCase(
  private val ticketRepository: TicketRepository,
  private val authRepository: AuthRepository
) {
  @OptIn(ExperimentalCoroutinesApi::class)
  operator fun invoke(): Flow<List<TicketListItem>> {
    return authRepository.authState.flatMapLatest { authState ->
      if (authState is AuthState.Authenticated) {
        combine(
          ticketRepository.getUserTickets(authState.user.id),
          ticketRepository.getDrafts()
        ) { tickets, drafts ->
          val remote = tickets.map { TicketListItem.Remote(it) }
          val local = drafts.map { TicketListItem.Local(it) }
          remote + local
        }
      } else {
        flowOf(emptyList())
      }
    }
  }
}