package com.example.community_app.dto

import com.example.community_app.util.*
import kotlinx.serialization.Serializable

@Serializable
data class TicketCreateDto(
  val title: String,
  val description: String? = null,
  val category: TicketCategory,
  val officeId: Int,
  val address: AddressDto? = null,
  val visibility: TicketVisibility = TicketVisibility.PUBLIC
)

@Serializable
data class TicketUpdateDto(
  val title: String? = null,
  val description: String? = null,
  val category: TicketCategory? = null,
  val officeId: Int? = null,
  val address: AddressDto? = null,
  val visibility: TicketVisibility? = null
)

@Serializable
data class TicketDto(
  val id: Int,
  val title: String,
  val description: String?,
  val category: TicketCategory,
  val officeId: Int?,
  val creatorUserId: Int,
  val address: AddressDto?,
  val visibility: TicketVisibility,
  val createdAt: String,
  val currentStatus: TicketStatusDto? = null,
  val votesCount: Int = 0,
  val userVoted: Boolean? = null,
  val media: List<MediaDto> = emptyList(),
  val imageUrl: String? = null
)

@Serializable
data class TicketStatusCreateDto(
  val status: TicketStatus,
  val message: String? = null
)

@Serializable
data class TicketStatusDto(
  val id: Int,
  val status: TicketStatus,
  val message: String?,
  val createdByUserId: Int?,
  val createdAt: String
)

@Serializable
data class TicketVoteSummaryDto(
  val ticketId: Int,
  val votes: Int,
  val userVoted: Boolean? = null
)
