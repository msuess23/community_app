package com.example.community_app.info.presentation.info_master

import com.example.community_app.core.domain.location.Location
import com.example.community_app.core.presentation.helpers.UiText
import com.example.community_app.info.domain.Info
import com.example.community_app.util.InfoCategory
import com.example.community_app.util.InfoStatus

data class InfoMasterState(
  val searchQuery: String = "",
  val filter: InfoFilterState = InfoFilterState(),
  val isFilterSheetVisible: Boolean = false,
  val searchResults: List<Info> = emptyList(),
  val isLoading: Boolean = false,
  val errorMessage: UiText? = null,
  val userLocation: Location? = null,
  val isPermissionGranted: Boolean = false
)

data class InfoFilterState(
  val selectedCategories: Set<InfoCategory> = emptySet(),
  val selectedStatuses: Set<InfoStatus> = emptySet(),
  val distanceRadiusKm: Float = 50f,
  val sortBy: InfoSortOption = InfoSortOption.DATE_DESC,
  val expandedSections: Set<FilterSection> = setOf(FilterSection.CATEGORY)
)

enum class InfoSortOption {
  DATE_DESC, DATE_ASC, ALPHABETICAL
}

enum class FilterSection {
  CATEGORY, STATUS, DISTANCE
}