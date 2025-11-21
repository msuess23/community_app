package com.example.community_app.info.presentation.info_master

import com.example.community_app.core.presentation.UiText
import com.example.community_app.info.domain.Info
import com.example.community_app.util.InfoCategory

data class InfoMasterState(
  val searchQuery: String = "",
  val filter: InfoFilterState = InfoFilterState(),
  val isFilterSheetVisible: Boolean = false,
  val searchResults: List<Info> = emptyList(),
  val isLoading: Boolean = false,
  val errorMessage: UiText? = null,
)

data class InfoFilterState(
  val selectedCategories: Set<InfoCategory> = emptySet(),
  val sortBy: InfoSortOption = InfoSortOption.DATE_DESC
)

enum class InfoSortOption {
  DATE_DESC, DATE_ASC, ALPHABETICAL
}