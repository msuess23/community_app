package com.example.community_app.dto

import com.example.community_app.util.InfoCategory
import kotlinx.serialization.Serializable

@Serializable
data class InfoCreateDto(
  val title: String,
  val description: String? = null,
  val category: InfoCategory,
  val officeId: Int? = null,
  val address: AddressDto? = null,
  val startsAt: String,
  val endsAt: String
)

@Serializable
data class InfoUpdateDto(
  val title: String? = null,
  val description: String? = null,
  val category: InfoCategory? = null,
  val officeId: Int? = null,
  val address: AddressDto? = null,
  val startsAt: String? = null,
  val endsAt: String? = null
)

@Serializable
data class InfoDto(
  val id: Int,
  val title: String,
  val description: String?,
  val category: InfoCategory,
  val officeId: Int?,
  val address: AddressDto?,
  val createdAt: String,
  val startsAt: String,
  val endsAt: String,
  val currentStatus: StatusDto? = null,
  val imageUrl: String? = null
)
