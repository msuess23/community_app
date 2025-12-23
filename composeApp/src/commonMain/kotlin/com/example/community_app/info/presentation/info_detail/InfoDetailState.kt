package com.example.community_app.info.presentation.info_detail

import com.example.community_app.core.presentation.helpers.UiText
import com.example.community_app.dto.InfoStatusDto
import com.example.community_app.info.domain.model.Info

data class InfoDetailState(
  val isLoading: Boolean = false,
  val info: Info? = null,
  val imageUrls: List<String> = emptyList(),
  val showStatusHistory: Boolean = false,
  val statusHistory: List<InfoStatusDto> = emptyList(),
  val errorMessage: UiText? = null,
  val isDescriptionExpanded: Boolean = false
)
