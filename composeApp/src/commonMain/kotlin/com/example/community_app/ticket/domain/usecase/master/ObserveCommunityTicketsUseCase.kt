package com.example.community_app.ticket.domain.usecase.master

import com.example.community_app.profile.domain.repository.UserRepository
import com.example.community_app.ticket.domain.model.Ticket
import com.example.community_app.ticket.domain.repository.TicketRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest

class ObserveCommunityTicketsUseCase(
  private val ticketRepository: TicketRepository,
  private val userRepository: UserRepository
) {
  @OptIn(ExperimentalCoroutinesApi::class)
  operator fun invoke(): Flow<List<Ticket>> {
    return userRepository.getUser().flatMapLatest { user ->
      if (user != null) {
        ticketRepository.getCommunityTickets(user.id)
      } else {
        ticketRepository.getTickets()
      }
    }
  }
}