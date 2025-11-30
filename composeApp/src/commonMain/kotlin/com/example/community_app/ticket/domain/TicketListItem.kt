package com.example.community_app.ticket.domain

sealed interface TicketListItem {
  data class Remote(val ticket: Ticket) : TicketListItem
  data class Local(val draft: TicketDraft) : TicketListItem
}