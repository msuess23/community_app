package com.example.community_app.office.domain.usecase

import com.example.community_app.core.domain.location.Location
import com.example.community_app.core.util.GeoUtil
import com.example.community_app.office.domain.Office

class FilterOfficesUseCase {
  operator fun invoke(
    offices: List<Office>,
    query: String,
    distanceKm: Float,
    userLocation: Location?
  ): List<Office> {
    var result = offices

    // 1. Text Search (Name, Description, Services)
    if (query.isNotBlank()) {
      result = result.filter {
        it.name.contains(query, ignoreCase = true) ||
            it.description?.contains(query, ignoreCase = true) == true ||
            it.services?.contains(query, ignoreCase = true) == true
      }
    }

    // 2. Distance
    if (userLocation != null) {
      result = result.filter { office ->
        val officeLoc = Location(office.address.latitude, office.address.longitude)
        val dist = GeoUtil.calculateDistanceKm(userLocation, officeLoc)
        dist <= distanceKm
      }
    }

    result = result.sortedBy { it.name } // TODO

    return result
  }
}