package com.example.community_app.info.presentation.info_detail

sealed interface InfoDetailAction {
  data object OnNavigateBack : InfoDetailAction
  data object OnShowStatusHistory : InfoDetailAction
  data object OnDismissStatusHistory : InfoDetailAction
}