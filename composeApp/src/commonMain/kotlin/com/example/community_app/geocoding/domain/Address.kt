package com.example.community_app.geocoding.domain

import kotlinx.serialization.Serializable

@Serializable
data class Address(
  val formatted: String? = null,
  val street: String? = null,
  val houseNumber: String? = null,
  val zipCode: String? = null,
  val city: String? = null,
  val country: String? = null,
  val longitude: Double,
  val latitude: Double
) {
  fun hasStructuredData(): Boolean {
    return !street.isNullOrBlank() || !city.isNullOrBlank()
  }

  fun getUiLine1(): String {
    val built = "${street ?: ""} ${houseNumber ?: ""}".trim()
    return built.ifBlank { formatted ?: "" }
  }

  fun getUiLine2(): String {
    return "${zipCode ?: ""} ${city ?: ""}".trim()
  }

  fun toOneLineString(): String {
    return formatted ?: listOfNotNull(
      "${street ?: ""} ${houseNumber ?: ""}".trim().ifBlank { null },
      zipCode,
      city,
      country
    ).joinToString(", ")
  }
}