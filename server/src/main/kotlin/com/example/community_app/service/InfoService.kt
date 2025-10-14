package com.example.community_app.service

import com.example.community_app.util.InfoCategory
import com.example.community_app.util.InfoStatus
import com.example.community_app.dto.*
import com.example.community_app.errors.NotFoundException
import com.example.community_app.repository.*
import com.example.community_app.util.ensureEndAfterStart
import com.example.community_app.util.parseBbox
import com.example.community_app.util.parseInstantStrict
import com.example.community_app.util.requireOfficerOfOrAdmin
import com.example.community_app.util.validateLocation
import io.ktor.server.auth.jwt.*

class InfoService(
  private val repo: InfoRepository,
  private val statusService: StatusService
) {

  // ---- Public reads ----

  suspend fun list(
    officeId: Int?, category: InfoCategory?,
    startsFrom: String?, endsTo: String?, bbox: String?
  ): List<InfoDto> {
    val from = startsFrom?.let { parseInstantStrict(it) }
    val to = endsTo?.let { parseInstantStrict(it) }
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
    validateLocation(dto.location)
    val s = parseInstantStrict(dto.startsAt)
    val e = parseInstantStrict(dto.endsAt)
    ensureEndAfterStart(s, e)

    requireOfficerOfOrAdmin(principal, dto.officeId)

    val rec = repo.create(
      InfoCreateData(
        title = dto.title,
        description = dto.description,
        category = dto.category,
        officeId = dto.officeId,
        location = dto.location,
        startsAt = s,
        endsAt = e
      )
    )

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
    val startsAt = patch.startsAt?.let { parseInstantStrict(it) }
    val endsAt = patch.endsAt?.let { parseInstantStrict(it) }
    if (startsAt != null && endsAt != null) ensureEndAfterStart(startsAt, endsAt)

    val updated = repo.update(
      id, InfoUpdateData(
        title = patch.title,
        description = patch.description,
        category = patch.category,
        officeId = patch.officeId,
        location = patch.location,
        startsAt = startsAt,
        endsAt = endsAt
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