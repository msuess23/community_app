package com.example.community_app.office.presentation.office_master

import com.example.community_app.core.presentation.components.search.FilterSection
import com.example.community_app.office.domain.model.Office

sealed interface OfficeMasterAction {
  data class OnSearchQueryChange(val query: String) : OfficeMasterAction
  data class OnOfficeClick(val office: Office) : OfficeMasterAction
  data object OnRefresh : OfficeMasterAction

  // Filter
  data object OnToggleFilterSheet : OfficeMasterAction
  data class OnDistanceChange(val distance: Float) : OfficeMasterAction
  data class OnSortChange(val option: OfficeSortOption) : OfficeMasterAction
  data class OnToggleSection(val section: FilterSection) : OfficeMasterAction
}