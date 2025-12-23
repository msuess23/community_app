package com.example.community_app.office.domain.model

import com.example.community_app.geocoding.domain.model.Address

data class Office(
  val id: Int,
  val name: String,
  val description: String?,
  val services: String?,
  val openingHours: String?,
  val contactEmail: String?,
  val phone: String?,
  val address: Address
)