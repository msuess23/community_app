package com.example.community_app.info.presentation.info_master

import com.example.community_app.info.domain.Info

sealed interface InfoMasterAction {
  data class OnSearchQueryChange(val query: String): InfoMasterAction
  data class OnInfoClick(val info: Info): InfoMasterAction
}