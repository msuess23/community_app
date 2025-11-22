package com.example.community_app.service

import com.example.community_app.util.InfoStatus
import com.example.community_app.util.StatusScope
import com.example.community_app.util.TicketStatus
import com.example.community_app.dto.InfoStatusDto
import com.example.community_app.dto.TicketStatusDto
import com.example.community_app.repository.DefaultStatusRepository
import com.example.community_app.repository.StatusRecord
import com.example.community_app.repository.StatusRepository

class StatusService(
  private val repo: StatusRepository
) {
  // -------- Generic --------
  suspend fun add(
    scope: StatusScope,
    scopeId: Int,
    statusText: String,
    message: String?,
    createdByUserId: Int?
  ): StatusRecord =
    repo.add(scope, scopeId, statusText, message, createdByUserId)

  suspend fun list(scope: StatusScope, scopeId: Int): List<StatusRecord> = repo.list(scope, scopeId)
  suspend fun latest(scope: StatusScope, scopeId: Int): StatusRecord? = repo.latest(scope, scopeId)

  // -------- Info helpers --------
  suspend fun addInfoStatus(infoId: Int, status: InfoStatus, message: String?, createdByUserId: Int?): InfoStatusDto =
    add(StatusScope.INFO, infoId, status.name, message, createdByUserId).toInfoStatusDtoOrThrow()

  suspend fun listInfoStatuses(infoId: Int): List<InfoStatusDto> =
    list(StatusScope.INFO, infoId).mapNotNull { it.toInfoStatusDtoOrNull() }

  suspend fun currentInfoStatus(infoId: Int): InfoStatusDto? =
    latest(StatusScope.INFO, infoId)?.toInfoStatusDtoOrNull()

  private fun StatusRecord.toInfoStatusDtoOrThrow(): InfoStatusDto =
    toInfoStatusDtoOrNull() ?: error("Invalid InfoStatus: $statusText")

  private fun StatusRecord.toInfoStatusDtoOrNull(): InfoStatusDto? =
    runCatching { InfoStatus.valueOf(statusText) }.map { st ->
      InfoStatusDto(id, st, message, createdByUserId, createdAt.toString())
    }.getOrNull()

  // -------- Ticket helpers --------
  suspend fun addTicketStatus(
    ticketId: Int,
    status: TicketStatus,
    message: String?,
    createdByUserId: Int?
  ): TicketStatusDto =
    add(StatusScope.TICKET, ticketId, status.name, message, createdByUserId).toTicketStatusDtoOrThrow()

  suspend fun listTicketStatuses(ticketId: Int): List<TicketStatusDto> =
    list(StatusScope.TICKET, ticketId).mapNotNull { it.toTicketStatusDtoOrNull() }

  suspend fun currentTicketStatus(ticketId: Int): TicketStatusDto? =
    latest(StatusScope.TICKET, ticketId)?.toTicketStatusDtoOrNull()

  private fun StatusRecord.toTicketStatusDtoOrThrow(): TicketStatusDto =
    toTicketStatusDtoOrNull() ?: error("Invalid TicketStatus: $statusText")

  private fun StatusRecord.toTicketStatusDtoOrNull(): TicketStatusDto? =
    runCatching { TicketStatus.valueOf(statusText) }.map { st ->
      TicketStatusDto(id, st, message, createdByUserId, createdAt.toString())
    }.getOrNull()

  companion object {
    fun default(): StatusService = StatusService(DefaultStatusRepository)
  }
}
