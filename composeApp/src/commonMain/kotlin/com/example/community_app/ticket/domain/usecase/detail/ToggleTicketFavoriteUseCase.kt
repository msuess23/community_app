package com.example.community_app.ticket.domain.usecase.detail

import com.example.community_app.ticket.domain.TicketRepository

class ToggleTicketFavoriteUseCase(
  private val ticketRepository: TicketRepository
) {
  suspend operator fun invoke(itemId: Int, isFavorite: Boolean) {
    ticketRepository.toggleFavorite(itemId, isFavorite)
  }
}