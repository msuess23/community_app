package com.example.community_app.info.presentation.info_detail

import com.example.community_app.info.domain.Info

data class InfoDetailState(
  val isLoading: Boolean = false,
  val info: Info? = null
)
