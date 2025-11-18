package com.example.community_app.info.presentation.info_master

import com.example.community_app.core.presentation.UiText
import com.example.community_app.info.domain.Info

data class InfoMasterState(
  val searchQuery: String = "",
  val searchResults: List<Info> = emptyList(),
  val isLoading: Boolean = false,
  val errorMessage: UiText? = null
)
