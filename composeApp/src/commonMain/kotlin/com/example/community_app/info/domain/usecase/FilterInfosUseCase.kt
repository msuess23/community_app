package com.example.community_app.info.domain.usecase

import com.example.community_app.core.domain.location.Location
import com.example.community_app.core.util.GeoUtil
import com.example.community_app.info.domain.Info
import com.example.community_app.info.presentation.info_master.InfoFilterState
import com.example.community_app.info.presentation.info_master.InfoSortOption

class FilterInfosUseCase() {
  operator fun invoke(
    infos: List<Info>,
    query: String,
    filter: InfoFilterState,
    userLocation: Location?
  ): List<Info> {
    var result = infos

    if (query.isNotBlank()) {
      result = result.filter {
        it.title.contains(query, ignoreCase = true) ||
            (it.description?.contains(query, ignoreCase = true) == true)
      }
    }

    if (filter.selectedCategories.isNotEmpty()) {
      result = result.filter {
        it.category in filter.selectedCategories
      }
    }

    if (filter.selectedStatuses.isNotEmpty()) {
      result = result.filter {
        val status = it.currentStatus
        status != null && status in filter.selectedStatuses
      }
    }

    if (userLocation != null && filter.distanceRadiusKm < 50f) {
      result = result.filter { info ->
        val infoAddr = info.address
        if (infoAddr != null) {
          val infoLoc = Location(infoAddr.latitude, infoAddr.longitude)
          val dist = GeoUtil.calculateDistanceKm(userLocation, infoLoc)
          dist <= filter.distanceRadiusKm
        } else true
      }
    }

    result = when(filter.sortBy) {
      InfoSortOption.DATE_DESC -> result.sortedByDescending { it.startsAt }
      InfoSortOption.DATE_ASC -> result.sortedBy { it.startsAt }
      InfoSortOption.ALPHABETICAL -> result.sortedBy { it.title }
      InfoSortOption.FAVORITES -> result.sortedByDescending { it.isFavorite }
      InfoSortOption.DISTANCE -> {
        if (userLocation != null) {
          result.sortedBy { info ->
            if (info.address != null) {
              val infoLoc = Location(info.address.latitude, info.address.longitude)
              GeoUtil.calculateDistanceKm(userLocation, infoLoc)
            } else {
              Double.MAX_VALUE
            }
          }
        } else {
          result
        }
      }
    }

    return result
  }
}