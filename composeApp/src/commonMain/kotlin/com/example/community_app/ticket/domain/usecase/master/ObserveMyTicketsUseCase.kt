package com.example.community_app.ticket.domain.usecase.master

import com.example.community_app.profile.domain.repository.UserRepository
import com.example.community_app.ticket.domain.model.TicketListItem
import com.example.community_app.ticket.domain.repository.TicketRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

class ObserveMyTicketsUseCase(
  private val ticketRepository: TicketRepository,
  private val userRepository: UserRepository
) {
  @OptIn(ExperimentalCoroutinesApi::class)
  operator fun invoke(): Flow<List<TicketListItem>> {
    return userRepository.getUser().flatMapLatest { user ->
      if (user != null) {
        combine(
          ticketRepository.getUserTickets(user.id),
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