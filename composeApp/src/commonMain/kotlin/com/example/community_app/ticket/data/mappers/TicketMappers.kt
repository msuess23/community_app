package com.example.community_app.ticket.data.mappers

import com.example.community_app.core.domain.model.Address
import com.example.community_app.dto.TicketDto
import com.example.community_app.ticket.domain.Ticket
import com.example.community_app.ticket.data.local.ticket.TicketAddressEntity
import com.example.community_app.ticket.data.local.ticket.TicketEntity
import com.example.community_app.util.BASE_URL
import com.example.community_app.util.TicketCategory
import com.example.community_app.util.TicketStatus
import com.example.community_app.util.TicketVisibility

fun TicketDto.toEntity(): TicketEntity {
  return TicketEntity(
    id = id,
    title = title,
    description = description,
    category = category.toString(),
    officeId = officeId,
    creatorUserId = creatorUserId,
    address = address?.let {
      TicketAddressEntity(
        street = it.street,
        houseNumber = it.houseNumber,
        zipCode = it.zipCode,
        city = it.city,
        longitude = it.longitude,
        latitude = it.latitude
      )
    },
    visibility = visibility.toString(),
    createdAt = createdAt,
    currentStatus = currentStatus?.status?.toString(),
    statusMessage = currentStatus?.message,
    votesCount = votesCount,
    userVoted = userVoted,
    imageUrl = imageUrl
  )
}

fun TicketEntity.toTicket(): Ticket {
  return Ticket(
    id = id,
    title = title,
    description = description,
    category = try {
      TicketCategory.valueOf(category)
    } catch (e: Exception) {
      TicketCategory.OTHER
    },
    officeId = officeId,
    creatorUserId = creatorUserId,
    address = address?.let {
      Address(
        street = it.street,
        houseNumber = it.houseNumber,
        zipCode = it.zipCode,
        city = it.city,
        longitude = it.longitude,
        latitude = it.latitude
      )
    },
    visibility = try {
      TicketVisibility.valueOf(visibility)
    } catch(e: Exception) {
      TicketVisibility.PUBLIC
    },
    createdAt = createdAt,
    currentStatus = currentStatus?.let {
      try { TicketStatus.valueOf(it) } catch(e: Exception) { null }
    },
    statusMessage = statusMessage,
    votesCount = votesCount,
    userVoted = userVoted,
    imageUrl = imageUrl?.let { "$BASE_URL$it" }
  )
}