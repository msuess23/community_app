package com.example.community_app.office.data.mappers

import com.example.community_app.core.domain.model.Address
import com.example.community_app.dto.OfficeDto
import com.example.community_app.office.data.local.OfficeAddressEntity
import com.example.community_app.office.data.local.OfficeEntity
import com.example.community_app.office.domain.Office

fun OfficeDto.toEntity(): OfficeEntity {
  return OfficeEntity(
    id = id,
    name = name,
    description = description,
    services = services,
    openingHours = openingHours,
    contactEmail = contactEmail,
    phone = phone,
    address = OfficeAddressEntity(
      street = address.street,
      houseNumber = address.houseNumber,
      zipCode = address.zipCode,
      city = address.city,
      longitude = address.longitude,
      latitude = address.latitude
    )
  )
}

fun OfficeEntity.toOffice(): Office {
  return Office(
    id = id,
    name = name,
    description = description,
    services = services,
    openingHours = openingHours,
    contactEmail = contactEmail,
    phone = phone,
    address = Address(
      street = address.street,
      houseNumber = address.houseNumber,
      zipCode = address.zipCode,
      city = address.city,
      longitude = address.longitude,
      latitude = address.latitude
    )
  )
}