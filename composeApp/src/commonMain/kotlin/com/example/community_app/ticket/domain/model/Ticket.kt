package com.example.community_app.ticket.domain.model

import com.example.community_app.geocoding.domain.model.Address
import com.example.community_app.util.TicketCategory
import com.example.community_app.util.TicketStatus
import com.example.community_app.util.TicketVisibility

data class Ticket(
  val id: Int,
  val title: String,
  val description: String?,
  val category: TicketCategory,
  val officeId: Int?,
  val creatorUserId: Int,
  val address: Address?,
  val visibility: TicketVisibility,
  val createdAt: String,
  val currentStatus: TicketStatus?,
  val statusMessage: String?,
  val votesCount: Int,
  val userVoted: Boolean?,
  val imageUrl: String?,
  val isFavorite: Boolean = false
)