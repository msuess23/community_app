package com.example.community_app.dto

import kotlinx.serialization.Serializable

@Serializable
data class MediaDto(
  val id: Int,
  val url: String,
  val mimeType: String,
  val width: Int? = null,
  val height: Int? = null,
  val sizeBytes: Long,
  val createdAt: String
)
