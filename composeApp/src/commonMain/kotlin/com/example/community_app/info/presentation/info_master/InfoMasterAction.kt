package com.example.community_app.info.presentation.info_master

import com.example.community_app.core.presentation.components.search.FilterSection
import com.example.community_app.info.domain.model.Info
import com.example.community_app.util.InfoCategory
import com.example.community_app.util.InfoStatus

sealed interface InfoMasterAction {
  data class OnSearchQueryChange(val query: String): InfoMasterAction
  data class OnInfoClick(val info: Info): InfoMasterAction
  data object OnRefresh : InfoMasterAction
  data object OnToggleFilterSheet : InfoMasterAction
  data class OnSortChange(val option: InfoSortOption) : InfoMasterAction
  data class OnCategorySelect(val category: InfoCategory) : InfoMasterAction
  data object OnClearCategories : InfoMasterAction
  data class OnStatusSelect(val status: InfoStatus) : InfoMasterAction
  data object OnClearStatuses : InfoMasterAction
  data class OnDistanceChange(val distance: Float) : InfoMasterAction
  data class OnToggleSection(val section: FilterSection) : InfoMasterAction
}