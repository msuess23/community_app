package com.example.community_app.dto

import kotlinx.serialization.Serializable

@Serializable
data class OfficeCreateDto(
  val name: String,
  val description: String? = null,
  val services: String? = null,
  val openingHours: String? = null,
  val contactEmail: String? = null,
  val phone: String? = null,
  val address: AddressDto
)

@Serializable
data class OfficeUpdateDto(
  val name: String? = null,
  val description: String? = null,
  val services: String? = null,
  val openingHours: String? = null,
  val contactEmail: String? = null,
  val phone: String? = null,
  val address: AddressDto? = null
)

@Serializable
data class OfficeDto(
  val id: Int,
  val name: String,
  val description: String?,
  val services: String?,
  val openingHours: String?,
  val contactEmail: String?,
  val phone: String?,
  val address: AddressDto
)
