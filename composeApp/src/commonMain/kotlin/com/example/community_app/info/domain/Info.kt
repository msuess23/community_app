package com.example.community_app.info.domain

import com.example.community_app.util.InfoCategory
import com.example.community_app.util.InfoStatus

data class Info(
  val id: Int,
  val title: String,
  val description: String?,
  val category: InfoCategory,
  val officeId: Int?,
  val address: InfoAddress?,
  val createdAt: String,
  val startsAt: String,
  val endsAt: String,
  val currentStatus: InfoStatus?,
  val statusMessage: String?,
  val imageUrl: String?
)

data class InfoAddress(
  val street: String?,
  val houseNumber: String?,
  val zipCode: String?,
  val city: String?,
  val longitude: Double,
  val latitude: Double
)