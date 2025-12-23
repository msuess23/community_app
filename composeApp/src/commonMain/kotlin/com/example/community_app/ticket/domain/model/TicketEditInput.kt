package com.example.community_app.ticket.domain.model

import com.example.community_app.geocoding.domain.model.Address
import com.example.community_app.util.TicketCategory
import com.example.community_app.util.TicketVisibility

data class TicketEditInput(
  val title: String,
  val description: String,
  val category: TicketCategory,
  val visibility: TicketVisibility,
  val officeId: Int?,
  val address: Address?,
  val images: List<EditableImage>
)