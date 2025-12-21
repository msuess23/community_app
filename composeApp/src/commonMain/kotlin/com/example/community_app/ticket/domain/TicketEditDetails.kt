package com.example.community_app.ticket.domain

import com.example.community_app.geocoding.domain.Address
import com.example.community_app.util.TicketCategory
import com.example.community_app.util.TicketVisibility

data class TicketEditDetails(
  val isDraft: Boolean,
  val ticketId: Int? = null,
  val draftId: Long? = null,
  val title: String = "",
  val description: String = "",
  val category: TicketCategory = TicketCategory.OTHER,
  val visibility: TicketVisibility = TicketVisibility.PRIVATE,
  val officeId: Int? = null,
  val address: Address ? = null,
  val images: List<EditableImage> = emptyList(),
  val coverImageUri: String? = null
)

data class EditableImage(
  val uri: String,
  val isLocal: Boolean,
  val id: String
)