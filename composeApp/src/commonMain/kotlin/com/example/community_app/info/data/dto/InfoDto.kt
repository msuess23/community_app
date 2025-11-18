package com.example.community_app.info.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class InfoDto(
  val id: Int,
  val title: String,
  val description: String?,
  val category: String,
  val officeId: Int?,
  // val address is an address
  val createdAt: String,
  val startsAt: String,
  val endsAt: String,
  // val currentStatus is a status
  val imageUrl: String? = null
)