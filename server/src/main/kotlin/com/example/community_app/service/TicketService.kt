package com.example.community_app.service

import com.example.community_app.util.*
import com.example.community_app.dto.*
import com.example.community_app.errors.NotFoundException
import com.example.community_app.errors.ValidationException
import com.example.community_app.repository.DefaultTicketRepository
import com.example.community_app.repository.TicketCreateData
import com.example.community_app.repository.TicketRecord
import com.example.community_app.repository.TicketRepository
import com.example.community_app.repository.TicketUpdateData
import com.example.community_app.util.ensureViewAllowedForVisibility
import com.example.community_app.util.requireEditByCreatorOfficerOrAdmin
import com.example.community_app.util.requireOfficerOfOrAdmin
import com.example.community_app.util.requireUserId
import io.ktor.server.auth.jwt.*
import java.time.Instant
import java.time.format.DateTimeParseException

class TicketService(
  private val repo: TicketRepository,
  private val statusService: StatusService,
  private val mediaService: MediaService
) {

  // -------- Public reads --------

  suspend fun listPublic(
    officeId: Int?, category: TicketCategory?, createdFrom: String?, createdTo: String?, bbox: String?, principal: JWTPrincipal?
  ): List<TicketDto> {
    val from = createdFrom?.let { parseInstant(it) }
    val to = createdTo?.let { parseInstant(it) }
    val bboxArr = bbox?.let { parseBbox(it) }
    val list = repo.listPublic(officeId, category, from, to, bboxArr)
    val userId = principal?.subject?.toIntOrNull()
    return list.map { toDto(it, userId) }
  }

  suspend fun getPublicAware(id: Int, principal: JWTPrincipal?): TicketDto {
    val rec = repo.findById(id) ?: throw NotFoundException("Ticket not found")
    ensureViewAllowedForVisibility(rec.visibility, rec.creatorUserId, rec.officeId, principal)
    val callerId = principal?.subject?.toIntOrNull()
    return toDto(rec, callerId)
  }

  // -------- Write --------

  suspend fun create(principal: JWTPrincipal, dto: TicketCreateDto): TicketDto {
    validateLocation(dto.location)
    val userId = principal.requireUserId()
    if (dto.title.isBlank()) throw ValidationException("title must not be blank")
    if (dto.officeId <= 0) throw ValidationException("officeId is required")

    val rec = repo.create(
      TicketCreateData(
        title = dto.title,
        description = dto.description,
        category = dto.category,
        officeId = dto.officeId,
        creatorUserId = userId,
        location = dto.location,
        visibility = dto.visibility
      )
    )

    // initial status: OPEN
    statusService.addTicketStatus(rec.id, TicketStatus.OPEN, "Created", userId)

    return toDto(rec, userId)
  }

  suspend fun update(principal: JWTPrincipal, id: Int, patch: TicketUpdateDto): TicketDto {
    val existing = repo.findById(id) ?: throw NotFoundException("Ticket not found")
    requireEditByCreatorOfficerOrAdmin(principal, existing.creatorUserId, existing.officeId)

    patch.location?.let { validateLocation(it) }

    val updated = repo.update(
      id, TicketUpdateData(
        title = patch.title,
        description = patch.description,
        category = patch.category,
        officeId = patch.officeId,
        location = patch.location,
        visibility = patch.visibility
      )
    ) ?: throw NotFoundException("Ticket not found")

    val callerId = principal.subject?.toIntOrNull()
    return toDto(updated, callerId)
  }

  suspend fun delete(principal: JWTPrincipal, id: Int) {
    val existing = repo.findById(id) ?: throw NotFoundException("Ticket not found")
    requireEditByCreatorOfficerOrAdmin(principal, existing.creatorUserId, existing.officeId)
    if (!repo.delete(id)) throw NotFoundException("Ticket not found")
  }

  // -------- Status --------

  suspend fun addStatus(principal: JWTPrincipal, id: Int, body: TicketStatusCreateDto): TicketStatusDto {
    val existing = repo.findById(id) ?: throw NotFoundException("Ticket not found")
    requireOfficerOfOrAdmin(principal, existing.officeId)
    val createdBy = principal.subject?.toIntOrNull()
    return statusService.addTicketStatus(id, body.status, body.message, createdBy)
  }

  suspend fun listStatus(id: Int, principal: JWTPrincipal?): List<TicketStatusDto> {
    val existing = repo.findById(id) ?: throw NotFoundException("Ticket not found")
    ensureViewAllowedForVisibility(existing.visibility, existing.creatorUserId, existing.officeId, principal)
    return statusService.listTicketStatuses(id)
  }

  suspend fun currentStatus(id: Int, principal: JWTPrincipal?): TicketStatusDto? {
    val existing = repo.findById(id) ?: throw NotFoundException("Ticket not found")
    ensureViewAllowedForVisibility(existing.visibility, existing.creatorUserId, existing.officeId, principal)
    return statusService.currentTicketStatus(id)
  }

  // -------- Votes --------

  suspend fun vote(principal: JWTPrincipal, id: Int): TicketVoteSummaryDto {
    val userId = principal.requireUserId()
    val existing = repo.findById(id) ?: throw NotFoundException("Ticket not found")
    ensureViewAllowedForVisibility(existing.visibility, existing.creatorUserId, existing.officeId, principal)

    repo.addVote(id, userId)
    val count = repo.countVotes(id)
    return TicketVoteSummaryDto(ticketId = id, votes = count, userVoted = true)
  }

  suspend fun unvote(principal: JWTPrincipal, id: Int): TicketVoteSummaryDto {
    val userId = principal.requireUserId()
    val existing = repo.findById(id) ?: throw NotFoundException("Ticket not found")
    ensureViewAllowedForVisibility(existing.visibility, existing.creatorUserId, existing.officeId, principal)

    repo.removeVote(id, userId)
    val count = repo.countVotes(id)
    val has = repo.userHasVoted(id, userId)
    return TicketVoteSummaryDto(ticketId = id, votes = count, userVoted = has)
  }

  suspend fun votesSummary(id: Int, principal: JWTPrincipal?): TicketVoteSummaryDto {
    val existing = repo.findById(id) ?: throw NotFoundException("Ticket not found")
    ensureViewAllowedForVisibility(existing.visibility, existing.creatorUserId, existing.officeId, principal)

    val count = repo.countVotes(id)
    val userId = principal?.subject?.toIntOrNull()
    val has = if (userId != null) repo.userHasVoted(id, userId) else null
    return TicketVoteSummaryDto(ticketId = id, votes = count, userVoted = has)
  }

  // -------- helpers --------

  private fun parseInstant(s: String): Instant =
    try { Instant.parse(s) } catch (e: DateTimeParseException) {
      throw ValidationException("Invalid datetime: $s (expected ISO-8601 UTC)")
    }

  private fun parseBbox(s: String): DoubleArray {
    val parts = s.split(",").map { it.trim() }
    if (parts.size != 4) throw ValidationException("bbox must be 'minLon,minLat,maxLon,maxLat'")
    return DoubleArray(4) { idx -> parts[idx].toDouble() }
  }

  private fun validateLocation(loc: LocationDto?) {
    if (loc == null) return
    if (loc.longitude !in -180.0..180.0) throw ValidationException("longitude out of range")
    if (loc.latitude !in -90.0..90.0) throw ValidationException("latitude out of range")
  }

  private suspend fun toDto(rec: TicketRecord, callerUserId: Int?): TicketDto {
    val current = statusService.currentTicketStatus(rec.id)
    val votes = repo.countVotes(rec.id)
    val userVoted = if (callerUserId != null) repo.userHasVoted(rec.id, callerUserId) else null
    val media = mediaService.listForTicket(rec.id) // dummy
    return rec.toDto(current, votes, userVoted, media)
  }

  private fun TicketRecord.toDto(
    current: TicketStatusDto?,
    votes: Int,
    userVoted: Boolean?,
    media: List<TicketMediaDto>
  ) = TicketDto(
    id = id,
    title = title,
    description = description,
    category = category,
    officeId = officeId,
    creatorUserId = creatorUserId,
    location = location?.let {
      LocationDto(
        longitude = it.longitude,
        latitude = it.latitude,
        altitude = it.altitude,
        accuracy = it.accuracy
      )
    },
    visibility = visibility,
    createdAt = createdAt.toString(),
    currentStatus = current,
    votesCount = votes,
    userVoted = userVoted,
    media = media
  )

  companion object {
    fun default(): TicketService = TicketService(
      repo = DefaultTicketRepository,
      statusService = StatusService.default(),
      mediaService = DefaultMediaService
    )
  }
}
