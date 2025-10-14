package com.example.community_app.service

import com.example.community_app.dto.TicketMediaDto

interface MediaService {
  suspend fun listForTicket(ticketId: Int): List<TicketMediaDto>
  suspend fun attachToTicket(ticketId: Int, media: List<TicketMediaDto>): List<TicketMediaDto>
  suspend fun detachFromTicket(ticketId: Int, mediaIds: List<String>): Boolean
}

object DefaultMediaService : MediaService {
  override suspend fun listForTicket(ticketId: Int): List<TicketMediaDto> = emptyList()
  override suspend fun attachToTicket(ticketId: Int, media: List<TicketMediaDto>): List<TicketMediaDto> = media
  override suspend fun detachFromTicket(ticketId: Int, mediaIds: List<String>): Boolean = true
}
