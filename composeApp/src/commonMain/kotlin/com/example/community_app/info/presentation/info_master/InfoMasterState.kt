package com.example.community_app.info.presentation.info_master

import com.example.community_app.core.presentation.components.search.FilterSection
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
  val errorMessage: UiText? = null
)

data class InfoFilterState(
  val selectedCategories: Set<InfoCategory> = emptySet(),
  val selectedStatuses: Set<InfoStatus> = emptySet(),
  val distanceRadiusKm: Float = 50f,
  val sortBy: InfoSortOption = InfoSortOption.DATE_DESC,
  val expandedSections: Set<FilterSection> = setOf(FilterSection.CATEGORY)
)

enum class InfoSortOption(val requiresLocation: Boolean = false) {
  DATE_DESC,
  DATE_ASC,
  ALPHABETICAL,
  FAVORITES,
  DISTANCE(requiresLocation = true)
}