package com.example.community_app.office.presentation.office_master

import com.example.community_app.core.domain.location.Location
import com.example.community_app.core.presentation.components.search.FilterSection
import com.example.community_app.core.presentation.helpers.UiText
import com.example.community_app.office.domain.model.Office

data class OfficeMasterState(
  val searchQuery: String = "",
  val filter: OfficeFilterState = OfficeFilterState(),
  val isFilterSheetVisible: Boolean = false,

  val searchResults: List<Office> = emptyList(),

  val isLoading: Boolean = false,
  val errorMessage: UiText? = null,
)

data class OfficeFilterState(
  val distanceRadiusKm: Float = 50f,
  val sortBy: OfficeSortOption = OfficeSortOption.ALPHABETICAL,
  val expandedSections: Set<FilterSection> = setOf(FilterSection.DISTANCE)
)

enum class OfficeSortOption {
  ALPHABETICAL, DISTANCE
}