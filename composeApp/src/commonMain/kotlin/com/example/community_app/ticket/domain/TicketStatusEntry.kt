package com.example.community_app.ticket.domain

import com.example.community_app.util.TicketStatus

data class TicketStatusEntry(
  val id: Int,
  val status: TicketStatus,
  val message: String?,
  val createdByUserId: Int?,
  val createdAt: String
)