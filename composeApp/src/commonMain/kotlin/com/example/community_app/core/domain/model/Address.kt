package com.example.community_app.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Address(
  val street: String? = null,
  val houseNumber: String? = null,
  val zipCode: String? = null,
  val city: String? = null,
  val longitude: Double,
  val latitude: Double
)