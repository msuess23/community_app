package com.example.community_app.core.domain.location

interface LocationService {
  suspend fun getCurrentLocation(): Location?
}

data class Location(
  val latitude: Double,
  val longitude: Double
)