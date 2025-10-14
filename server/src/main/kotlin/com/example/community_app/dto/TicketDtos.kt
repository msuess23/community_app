package com.example.community_app.dto

import com.example.community_app.util.*
import kotlinx.serialization.Serializable

@Serializable
data class TicketCreateDto(
  val title: String,
  val description: String? = null,
  val category: TicketCategory,
  val officeId: Int,                 // Ticket muss einer Office zugeordnet werden
  val location: LocationDto? = null,
  val visibility: TicketVisibility = TicketVisibility.PUBLIC
)

@Serializable
data class TicketUpdateDto(
  val title: String? = null,
  val description: String? = null,
  val category: TicketCategory? = null,
  val officeId: Int? = null,
  val location: LocationDto? = null,
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
  val location: LocationDto?,
  val visibility: TicketVisibility,
  val createdAt: String,
  val currentStatus: TicketStatusDto? = null,
  val votesCount: Int = 0,
  val userVoted: Boolean? = null,    // nur gesetzt, wenn User bekannt
  val media: List<TicketMediaDto> = emptyList()
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

// Platzhalter für späteren Media-Upload
@Serializable
data class TicketMediaDto(
  val id: String,
  val url: String,
  val mimeType: String
)

@Serializable
data class TicketVoteSummaryDto(
  val ticketId: Int,
  val votes: Int,
  val userVoted: Boolean? = null
)
