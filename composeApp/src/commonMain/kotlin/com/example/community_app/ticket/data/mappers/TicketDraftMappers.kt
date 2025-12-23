package com.example.community_app.ticket.data.mappers

import com.example.community_app.geocoding.domain.model.Address
import com.example.community_app.ticket.data.local.draft.TicketDraftEntity
import com.example.community_app.ticket.data.local.draft.TicketDraftWithImages
import com.example.community_app.ticket.data.local.ticket.TicketAddressEntity
import com.example.community_app.ticket.domain.model.TicketDraft
import com.example.community_app.util.TicketCategory
import com.example.community_app.util.TicketVisibility

fun TicketDraft.toEntity(userId: Int): TicketDraftEntity {
  return TicketDraftEntity(
    id = id,
    title = title,
    description = description,
    category = category?.name,
    officeId = officeId,
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
    visibility = visibility.name,
    lastModified = lastModified,
    userId = userId
  )
}

fun TicketDraftWithImages.toTicketDraft(): TicketDraft {
  return TicketDraft(
    id = draft.id,
    title = draft.title,
    description = draft.description,
    category = draft.category?.let {
      runCatching { TicketCategory.valueOf(it) }.getOrNull()
    },
    officeId = draft.officeId,
    address = draft.address?.let {
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
      TicketVisibility.valueOf(draft.visibility)
    } catch (e: Exception) {
      TicketVisibility.PRIVATE
    },
    images = images.map { it.localUri },
    lastModified = draft.lastModified
  )
}