package com.example.community_app.info.presentation.info_master

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class InfoMasterViewModel: ViewModel() {
  private val _state = MutableStateFlow(InfoMasterState())
  val state = _state.asStateFlow()

  fun onAction(action: InfoMasterAction) {
    when(action) {
      is InfoMasterAction.OnInfoClick -> {}
      is InfoMasterAction.OnSearchQueryChange -> {
        _state.update {
          it.copy(searchQuery = action.query)
        }
      }
    }
  }
}