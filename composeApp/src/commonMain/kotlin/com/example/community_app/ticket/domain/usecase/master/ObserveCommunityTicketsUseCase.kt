package com.example.community_app.ticket.domain.usecase.master

import com.example.community_app.auth.domain.AuthRepository
import com.example.community_app.auth.domain.AuthState
import com.example.community_app.ticket.domain.Ticket
import com.example.community_app.ticket.domain.TicketRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest

class ObserveCommunityTicketsUseCase(
  private val ticketRepository: TicketRepository,
  private val authRepository: AuthRepository
) {
  @OptIn(ExperimentalCoroutinesApi::class)
  operator fun invoke(): Flow<List<Ticket>> {
    return authRepository.authState.flatMapLatest { authState ->
      if (authState is AuthState.Authenticated) {
        ticketRepository.getCommunityTickets(authState.user.id)
      } else {
        ticketRepository.getTickets()
      }
    }
  }
}