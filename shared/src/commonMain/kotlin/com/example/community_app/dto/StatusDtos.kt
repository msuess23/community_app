package com.example.community_app.dto

import com.example.community_app.util.InfoStatus
import kotlinx.serialization.Serializable

@Serializable
data class InfoStatusCreateDto(
  val status: InfoStatus,
  val message: String? = null
)

@Serializable
data class InfoStatusDto(
  val id: Int,
  val status: InfoStatus,
  val message: String?,
  val createdByUserId: Int?,
  val createdAt: String
)
