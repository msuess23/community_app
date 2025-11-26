package com.example.community_app.ticket.domain

import com.example.community_app.core.domain.model.Address
import com.example.community_app.util.TicketCategory
import com.example.community_app.util.TicketVisibility

data class TicketDraft(
  val id: Long = 0,
  val title: String = "",
  val description: String? = null,
  val category: TicketCategory? = null,
  val officeId: Int? = null,
  val address: Address? = null,
  val visibility: TicketVisibility = TicketVisibility.PRIVATE,
  val localImageUri: String? = null,
  val lastModified: String
)
