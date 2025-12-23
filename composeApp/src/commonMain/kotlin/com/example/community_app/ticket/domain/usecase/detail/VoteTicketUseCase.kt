package com.example.community_app.ticket.domain.usecase.detail

import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.ticket.domain.repository.TicketRepository

class VoteTicketUseCase(
  private val ticketRepository: TicketRepository
) {
  suspend operator fun invoke(
    ticketId: Int,
    currentlyVoted: Boolean
  ): Result<Unit, DataError.Remote> {
    return if (currentlyVoted) {
      ticketRepository.unvoteTicket(ticketId)
    } else {
      ticketRepository.voteTicket(ticketId)
    }
  }
}