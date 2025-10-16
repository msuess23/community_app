package com.example.community_app.dto

import kotlinx.serialization.Serializable

@Serializable
data class SlotCreateDto(
  val startsAt: String,
  val endsAt: String
)

@Serializable
data class SlotBatchCreateDto(
  val slots: List<SlotCreateDto>
)

@Serializable
data class SlotDto(
  val id: Int,
  val startsAt: String,
  val endsAt: String
)

@Serializable
data class AppointmentCreateDto(
  val startsAt: String,
  val endsAt: String
)

@Serializable
data class AppointmentDto(
  val id: Int,
  val officeId: Int,
  val startsAt: String,
  val endsAt: String,
  val userId: Int?
)
