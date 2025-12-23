package com.example.community_app.ticket.domain.model

import com.example.community_app.geocoding.domain.model.Address
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
  val images: List<String> = emptyList(),
  val lastModified: String
)
