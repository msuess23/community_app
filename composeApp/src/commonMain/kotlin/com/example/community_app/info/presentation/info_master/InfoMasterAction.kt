package com.example.community_app.info.presentation.info_master

import com.example.community_app.info.domain.Info
import com.example.community_app.util.InfoCategory

sealed interface InfoMasterAction {
  data class OnSearchQueryChange(val query: String): InfoMasterAction
  data class OnInfoClick(val info: Info): InfoMasterAction
  data object OnRefresh : InfoMasterAction
  data object OnToggleFilterSheet : InfoMasterAction
  data class OnCategorySelect(val category: InfoCategory) : InfoMasterAction
  data object OnClearCategories : InfoMasterAction
  data class OnSortChange(val option: InfoSortOption) : InfoMasterAction
}