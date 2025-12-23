package com.example.community_app.office.domain.usecase

import com.example.community_app.core.domain.location.Location
import com.example.community_app.geocoding.domain.model.Address
import com.example.community_app.core.util.GeoUtil
import com.example.community_app.office.domain.model.Office
import com.example.community_app.office.presentation.office_master.OfficeFilterState
import com.example.community_app.office.presentation.office_master.OfficeSortOption

class FilterOfficesUseCase() {
  operator fun invoke(
    offices: List<Office>,
    query: String,
    filter: OfficeFilterState,
    userLocation: Location?
  ): List<Office> {
    var result = offices

    if (query.isNotBlank()) {
      result = result.filter {
        it.name.contains(query, ignoreCase = true) ||
            it.description?.contains(query, ignoreCase = true) == true ||
            it.services?.contains(query, ignoreCase = true) == true
      }
    }

    if (userLocation != null && filter.distanceRadiusKm < 50f) {
      result = result.filter { office ->
        val dist = calculateDistance(userLocation, office.address)
        dist <= filter.distanceRadiusKm
      }
    }

    result = when(filter.sortBy) {
      OfficeSortOption.ALPHABETICAL -> result.sortedBy { it.name }
      OfficeSortOption.DISTANCE -> {
        if (userLocation != null) {
          result.sortedBy { office ->
            calculateDistance(userLocation, office.address)
          }
        } else {
          result.sortedBy { it.name }
        }
      }
    }

    return result
  }

  private fun calculateDistance(
    userLocation: Location,
    officeAddress: Address
  ): Double {
    val officeLoc = Location(officeAddress.latitude, officeAddress.longitude)
    return GeoUtil.calculateDistanceKm(userLocation, officeLoc)
  }
}