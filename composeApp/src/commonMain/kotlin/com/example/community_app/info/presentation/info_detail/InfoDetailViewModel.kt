package com.example.community_app.info.presentation.info_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.community_app.app.Route
import com.example.community_app.core.domain.Result
import com.example.community_app.info.domain.InfoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InfoDetailViewModel(
  savedStateHandle: SavedStateHandle,
  private val infoRepository: InfoRepository
) : ViewModel() {
  private val infoId = savedStateHandle.toRoute<Route.InfoDetail>().id

  private val _state = MutableStateFlow(InfoDetailState())
  val state = _state
    .onStart {
      observeInfo()
      fetchUpdate()
    }
    .stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000L),
      _state.value
    )

  private fun observeInfo() {
    infoRepository.getInfo(infoId)
      .onEach { info ->
        _state.update { it.copy(info = info) }
      }
      .launchIn(viewModelScope)
  }

  private fun fetchUpdate() {
    viewModelScope.launch {
      if (_state.value.info == null) {
        _state.update { it.copy(isLoading = true) }
      }

      val result = infoRepository.refreshInfo(infoId)
      _state.update { it.copy(isLoading = false) }

      if (result is Result.Error) {
        println("Detail refresh failed: ${result.error}")
      }
    }
  }
}