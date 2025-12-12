package com.example.community_app.ticket.domain.usecase

import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.ticket.domain.TicketRepository

class VoteTicketUseCase(
  private val ticketRepository: TicketRepository
) {
  suspend operator fun invoke(ticketId: Int, vote: Boolean): Result<Unit, DataError.Remote> {
    return if (vote) {
      ticketRepository.voteTicket(ticketId)
    } else {
      ticketRepository.unvoteTicket(ticketId)
    }
  }
}