package com.example.community_app.core.presentation.state

import com.example.community_app.core.presentation.helpers.UiText

data class UiControlState(
  val isLoading: Boolean = false,
  val errorMessage: UiText? = null,
  val isFilterSheetVisible: Boolean = false
)