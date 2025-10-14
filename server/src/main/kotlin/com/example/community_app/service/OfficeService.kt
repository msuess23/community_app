package com.example.community_app.service

import com.example.community_app.dto.LocationDto
import com.example.community_app.dto.*
import com.example.community_app.errors.NotFoundException
import com.example.community_app.errors.ValidationException
import com.example.community_app.repository.*

class OfficeService(
  private val repo: OfficeRepository
) {
  suspend fun list(q: String?, bboxParam: String?): List<OfficeDto> {
    val bbox = bboxParam?.let { parseBbox(it) }
    return repo.list(q, bbox).map { it.toDto() }
  }

  suspend fun get(id: Int): OfficeDto {
    val office = repo.findById(id) ?: throw NotFoundException("Office not found")
    return office.toDto()
  }

  suspend fun create(dto: OfficeCreateDto): OfficeDto {
    validateLocation(dto.location)
    val rec = repo.create(
      OfficeCreateData(
        name = dto.name,
        description = dto.description,
        services = dto.services,
        openingHours = dto.openingHours,
        contactEmail = dto.contactEmail,
        phone = dto.phone,
        location = dto.location
      )
    )
    return rec.toDto()
  }

  suspend fun update(id: Int, patch: OfficeUpdateDto): OfficeDto {
    patch.location?.let { validateLocation(it) }
    val updated = repo.update(
      id,
      OfficeUpdateData(
        name = patch.name,
        description = patch.description,
        services = patch.services,
        openingHours = patch.openingHours,
        contactEmail = patch.contactEmail,
        phone = patch.phone,
        location = patch.location
      )
    ) ?: throw NotFoundException("Office not found")
    return updated.toDto()
  }

  suspend fun delete(id: Int) {
    repo.delete(id)
  }

  // --- helpers ---
  private fun parseBbox(s: String): DoubleArray {
    val parts = s.split(",").map { it.trim() }
    if (parts.size != 4) throw ValidationException("bbox must be 'minLon,minLat,maxLon,maxLat'")
    return DoubleArray(4) { idx -> parts[idx].toDouble() }
  }

  private fun validateLocation(loc: LocationDto) {
    if (loc.longitude !in -180.0..180.0) throw ValidationException("longitude out of range")
    if (loc.latitude !in -90.0..90.0) throw ValidationException("latitude out of range")
  }

  private fun OfficeRecord.toDto() = OfficeDto(
    id = id,
    name = name,
    description = description,
    services = services,
    openingHours = openingHours,
    contactEmail = contactEmail,
    phone = phone,
    location = LocationDto(
      longitude = location.longitude,
      latitude = location.latitude,
      altitude = location.altitude,
      accuracy = location.accuracy
    )
  )
}
