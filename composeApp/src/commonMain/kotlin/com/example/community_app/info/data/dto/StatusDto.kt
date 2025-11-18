package com.example.community_app.info.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class StatusDto(
  val id: Int,
  val status: String,
  val message: String?,
  val createdByUserId: Int?,
  val createdAt: String
)