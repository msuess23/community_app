package com.example.community_app.info.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class AddressDto(
  val street: String? = null,
  val houseNumber: String? = null,
  val zipCode: String? = null,
  val city: String? = null,
  val longitude: Double,
  val latitude: Double
)