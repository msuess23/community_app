package com.example.community_app.service

import com.example.community_app.dto.*
import com.example.community_app.errors.NotFoundException
import com.example.community_app.repository.*
import com.example.community_app.util.parseBbox
import com.example.community_app.util.toDto
import com.example.community_app.util.validateAddress

class OfficeService(
  private val repo: OfficeRepository
) {
  suspend fun list(q: String?, bboxParam: String?): List<OfficeDto> {
    val bbox = bboxParam?.let { parseBbox(it) }
    return repo.list(q, bbox).map { it.toDto() }
  }

  suspend fun get(id: Int): OfficeDto =
    repo.findById(id)?.toDto() ?: throw NotFoundException("Office not found")

  suspend fun create(body: OfficeCreateDto): OfficeDto {
    validateAddress(body.address)
    val rec = repo.create(
      OfficeCreateData(
        name = body.name,
        description = body.description,
        services = body.services,
        openingHours = body.openingHours,
        contactEmail = body.contactEmail,
        phone = body.phone,
        address = body.address // Mapping DTO property name
      )
    )
    return rec.toDto()
  }

  suspend fun update(id: Int, patch: OfficeUpdateDto): OfficeDto {
    patch.address?.let { validateAddress(it) }
    val updated = repo.update(
      id,
      OfficeUpdateData(
        name = patch.name,
        description = patch.description,
        services = patch.services,
        openingHours = patch.openingHours,
        contactEmail = patch.contactEmail,
        phone = patch.phone,
        address = patch.address
      )
    ) ?: throw NotFoundException("Office not found")
    return updated.toDto()
  }

  suspend fun delete(id: Int) {
    repo.delete(id)
  }

  private fun OfficeRecord.toDto() = OfficeDto(
    id = id,
    name = name,
    description = description,
    services = services,
    openingHours = openingHours,
    contactEmail = contactEmail,
    phone = phone,
    address = address.toDto() // nutzt Mapper aus Mappers.kt
  )
}