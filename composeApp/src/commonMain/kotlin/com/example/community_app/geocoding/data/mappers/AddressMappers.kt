package com.example.community_app.geocoding.data.mappers

import com.example.community_app.core.util.getCurrentTimeMillis
import com.example.community_app.dto.AddressDto
import com.example.community_app.geocoding.data.local.AddressEntity
import com.example.community_app.geocoding.domain.model.Address

fun Address.toEntity(userId: Int, type: String?) = AddressEntity(
  userId = userId,
  formatted = formatted ?: toOneLineString(),
  street = street,
  houseNumber = houseNumber,
  zipCode = zipCode,
  city = city,
  country = country,
  latitude = latitude,
  longitude = longitude,
  type = type,
  lastUsedAt = getCurrentTimeMillis()
)

fun AddressEntity.toAddress() = Address(
  formatted = formatted,
  street = street,
  houseNumber = houseNumber,
  zipCode = zipCode,
  city = city,
  country = country,
  latitude = latitude,
  longitude = longitude
)

fun Address.toDto(): AddressDto {
  return AddressDto(
    street = this.street,
    houseNumber = this.houseNumber,
    zipCode = this.zipCode,
    city = this.city,
    latitude = this.latitude,
    longitude = this.longitude
  )
}