package com.example.community_app.geocoding.data.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GeoapifyResponseDto(
  val features: List<GeoapifyFeatureDto>
)

@Serializable
data class GeoapifyFeatureDto(
  val properties: GeoapifyPropertiesDto,
  val geometry: GeoapifyGeometryDto
)

@Serializable
data class GeoapifyGeometryDto(
  val coordinates: List<Double>
)

@Serializable
data class GeoapifyPropertiesDto(
  val formatted: String? = null,
  @SerialName("address_line1") val addressLine1: String? = null,
  @SerialName("address_line2") val addressLine2: String? = null,
  val street: String? = null,
  val housenumber: String? = null,
  val postcode: String? = null,
  val city: String? = null,
  val country: String? = null,
  val lat: Double,
  val lon: Double
)