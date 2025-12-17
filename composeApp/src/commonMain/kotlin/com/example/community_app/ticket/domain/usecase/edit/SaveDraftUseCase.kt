package com.example.community_app.ticket.domain.usecase.edit

import com.example.community_app.ticket.domain.TicketDraft
import com.example.community_app.ticket.domain.TicketRepository

class SaveDraftUseCase(
  private val ticketRepository: TicketRepository
) {
  suspend operator fun invoke(draft: TicketDraft): Long {
    return ticketRepository.saveDraft(draft)
  }
}