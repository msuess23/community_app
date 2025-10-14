package com.example.community_app.service

import com.example.community_app.util.InfoStatus
import com.example.community_app.util.StatusScope
import com.example.community_app.dto.StatusDto
import com.example.community_app.repository.DefaultStatusRepository
import com.example.community_app.repository.StatusRecord
import com.example.community_app.repository.StatusRepository

class StatusService(
  private val repo: StatusRepository
) {
  // -------- Generic API (für alle Scopes) --------

  suspend fun add(
    scope: StatusScope,
    scopeId: Int,
    statusText: String,
    message: String?,
    createdByUserId: Int?
  ): StatusRecord = repo.add(scope, scopeId, statusText, message, createdByUserId)

  suspend fun list(scope: StatusScope, scopeId: Int): List<StatusRecord> =
    repo.list(scope, scopeId)

  suspend fun latest(scope: StatusScope, scopeId: Int): StatusRecord? =
    repo.latest(scope, scopeId)

  // -------- Convenience: Info-typisierte Helfer --------
  // (Spiegeln wir später für TicketStatus als addTicketStatus/listTicketStatuses/currentTicketStatus)

  suspend fun addInfoStatus(infoId: Int, status: InfoStatus, message: String?, createdByUserId: Int?): StatusDto {
    val rec = add(StatusScope.INFO, infoId, status.name, message, createdByUserId)
    return rec.toInfoStatusDtoOrThrow()
  }

  suspend fun listInfoStatuses(infoId: Int): List<StatusDto> =
    list(StatusScope.INFO, infoId).mapNotNull { it.toInfoStatusDtoOrNull() }

  suspend fun currentInfoStatus(infoId: Int): StatusDto? =
    latest(StatusScope.INFO, infoId)?.toInfoStatusDtoOrNull()

  // -------- Mapping-Helfer --------

  private fun StatusRecord.toInfoStatusDtoOrThrow(): StatusDto =
    toInfoStatusDtoOrNull()
      ?: error("Invalid info statusText='$statusText' (not a valid InfoStatus)")

  private fun StatusRecord.toInfoStatusDtoOrNull(): StatusDto? =
    runCatching { InfoStatus.valueOf(statusText) }.map { st ->
      StatusDto(
        id = id,
        status = st,
        message = message,
        createdByUserId = createdByUserId,
        createdAt = createdAt.toString()
      )
    }.getOrNull()

  companion object {
    fun default(): StatusService = StatusService(DefaultStatusRepository)
  }
}
