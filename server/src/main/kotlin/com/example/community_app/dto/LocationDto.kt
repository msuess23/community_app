package com.example.community_app.dto

import kotlinx.serialization.Serializable

@Serializable
data class LocationDto(
  val longitude: Double,
  val latitude: Double,
  val altitude: Double? = null,
  val accuracy: Double? = null
)