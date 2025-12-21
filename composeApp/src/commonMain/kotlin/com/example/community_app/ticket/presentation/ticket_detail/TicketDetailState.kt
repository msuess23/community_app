package com.example.community_app.ticket.presentation.ticket_detail

import com.example.community_app.core.presentation.helpers.UiText
import com.example.community_app.ticket.domain.Ticket
import com.example.community_app.ticket.domain.TicketDraft
import com.example.community_app.ticket.domain.TicketStatusEntry

data class TicketDetailState(
  val isLoading: Boolean = false,
  val ticket: Ticket? = null,
  val draft: TicketDraft? = null,
  val isDraft: Boolean = false,
  val isOwner: Boolean = false,
  val imageUrls: List<String> = emptyList(),
  val showStatusHistory: Boolean = false,
  val statusHistory: List<TicketStatusEntry> = emptyList(),
  val errorMessage: UiText? = null
)