package com.example.community_app.service

import com.example.community_app.util.InfoCategory
import com.example.community_app.util.InfoStatus
import com.example.community_app.dto.LocationDto
import com.example.community_app.dto.*
import com.example.community_app.errors.NotFoundException
import com.example.community_app.errors.ValidationException
import com.example.community_app.repository.DefaultInfoRepository
import com.example.community_app.repository.InfoCreateData
import com.example.community_app.repository.InfoRecord
import com.example.community_app.repository.InfoRepository
import com.example.community_app.repository.InfoUpdateData
import com.example.community_app.util.requireOfficerOfOrAdmin
import io.ktor.server.auth.jwt.*
import java.time.Instant
import java.time.format.DateTimeParseException

class InfoService(
  private val repo: InfoRepository,
  private val statusService: StatusService
) {

  // ---- Public reads ----

  suspend fun list(
    officeId: Int?, category: InfoCategory?,
    startsFrom: String?, endsTo: String?, bbox: String?
  ): List<InfoDto> {
    val from = startsFrom?.let { parseInstant(it) }
    val to = endsTo?.let { parseInstant(it) }
    val bboxArr = bbox?.let { parseBbox(it) }
    val infos = repo.list(officeId, category, from, to, bboxArr)

    return infos.map { rec ->
      val current = statusService.currentInfoStatus(rec.id)
      rec.toDto(current)
    }
  }

  suspend fun get(id: Int): InfoDto {
    val rec = repo.findById(id) ?: throw NotFoundException("Info not found")
    val current = statusService.currentInfoStatus(id)
    return rec.toDto(current)
  }

  suspend fun listStatus(infoId: Int): List<StatusDto> {
    ensureInfoExists(infoId)
    return statusService.listInfoStatuses(infoId)
  }

  suspend fun getCurrentStatus(infoId: Int): StatusDto? {
    ensureInfoExists(infoId)
    return statusService.currentInfoStatus(infoId)
  }

  // ---- Write (Officer/Admin) ----

  suspend fun create(principal: JWTPrincipal, dto: InfoCreateDto): InfoDto {
    validateTimes(dto.startsAt, dto.endsAt)
    validateLocation(dto.location)

    // Admin always; Officer only if officeId matches (and not null)
    requireOfficerOfOrAdmin(principal, dto.officeId)

    val rec = repo.create(
      InfoCreateData(
        title = dto.title,
        description = dto.description,
        category = dto.category,
        officeId = dto.officeId,
        location = dto.location,
        startsAt = Instant.parse(dto.startsAt),
        endsAt = Instant.parse(dto.endsAt)
      )
    )

    // initial status: SCHEDULED
    val createdBy = principal.subject?.toIntOrNull()
    statusService.addInfoStatus(rec.id, InfoStatus.SCHEDULED, "Created", createdBy)

    val current = statusService.currentInfoStatus(rec.id)
    return rec.toDto(current)
  }

  suspend fun update(principal: JWTPrincipal, id: Int, patch: InfoUpdateDto): InfoDto {
    val existing = repo.findById(id) ?: throw NotFoundException("Info not found")
    val targetOfficeId = patch.officeId ?: existing.officeId
    requireOfficerOfOrAdmin(principal, targetOfficeId)

    patch.location?.let { validateLocation(it) }
    patch.startsAt?.let { parseInstant(it) }
    patch.endsAt?.let { parseInstant(it) }

    val updated = repo.update(
      id, InfoUpdateData(
        title = patch.title,
        description = patch.description,
        category = patch.category,
        officeId = patch.officeId,
        location = patch.location,
        startsAt = patch.startsAt?.let { Instant.parse(it) },
        endsAt = patch.endsAt?.let { Instant.parse(it) }
      )
    ) ?: throw NotFoundException("Info not found")

    val current = statusService.currentInfoStatus(id)
    return updated.toDto(current)
  }

  suspend fun delete(principal: JWTPrincipal, id: Int) {
    val existing = repo.findById(id) ?: throw NotFoundException("Info not found")
    requireOfficerOfOrAdmin(principal, existing.officeId)
    if (!repo.delete(id)) throw NotFoundException("Info not found")
  }

  suspend fun addStatus(principal: JWTPrincipal, id: Int, body: StatusCreateDto): StatusDto {
    val existing = repo.findById(id) ?: throw NotFoundException("Info not found")
    requireOfficerOfOrAdmin(principal, existing.officeId)

    val createdBy = principal.subject?.toIntOrNull()
    return statusService.addInfoStatus(id, body.status, body.message, createdBy)
  }

  // ---- helpers ----

  private suspend fun ensureInfoExists(id: Int) {
    val exists = repo.findById(id) != null
    if (!exists) throw NotFoundException("Info not found")
  }

  private fun parseInstant(s: String): Instant =
    try { Instant.parse(s) } catch (e: DateTimeParseException) {
      throw ValidationException("Invalid datetime: $s (expected ISO-8601 UTC)")
    }

  private fun parseBbox(s: String): DoubleArray {
    val parts = s.split(",").map { it.trim() }
    if (parts.size != 4) throw ValidationException("bbox must be 'minLon,minLat,maxLon,maxLat'")
    return DoubleArray(4) { idx -> parts[idx].toDouble() }
  }

  private fun validateTimes(startsAt: String, endsAt: String) {
    val s = parseInstant(startsAt)
    val e = parseInstant(endsAt)
    if (!e.isAfter(s)) throw ValidationException("endsAt must be after startsAt")
  }

  private fun validateLocation(loc: LocationDto?) {
    if (loc == null) return
    if (loc.longitude !in -180.0..180.0) throw ValidationException("longitude out of range")
    if (loc.latitude !in -90.0..90.0) throw ValidationException("latitude out of range")
  }

  private fun InfoRecord.toDto(current: StatusDto?): InfoDto =
    InfoDto(
      id = id,
      title = title,
      description = description,
      category = category,
      officeId = officeId,
      location = location?.let {
        LocationDto(
          longitude = it.longitude,
          latitude = it.latitude,
          altitude = it.altitude,
          accuracy = it.accuracy
        )
      },
      createdAt = createdAt.toString(),
      startsAt = startsAt.toString(),
      endsAt = endsAt.toString(),
      currentStatus = current
    )

  companion object {
    fun default(): InfoService = InfoService(
      repo = DefaultInfoRepository,
      statusService = StatusService.default()
    )
  }
}
