package com.example.community_app.info.presentation.info_master

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.community_app.core.domain.onError
import com.example.community_app.core.domain.onSuccess
import com.example.community_app.core.presentation.toUiText
import com.example.community_app.info.domain.Info
import com.example.community_app.info.domain.InfoRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InfoMasterViewModel(
  private val infoRepository: InfoRepository
): ViewModel() {
  private var cachedInfos = emptyList<Info>()
  private var searchJob: Job? = null

  private val _state = MutableStateFlow(InfoMasterState())
  val state = _state
    .onStart {
      if(cachedInfos.isEmpty()) {
        observeSearchQuery()
      }
    }
    .stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000L),
      _state.value
    )

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

  @OptIn(FlowPreview::class)
  private fun observeSearchQuery() {
    state
      .map { it.searchQuery }
      .distinctUntilChanged()
      .debounce(500L)
      .onEach { query ->
        when {
          query.isBlank() -> {
            _state.update { it.copy(
              errorMessage = null,
              searchResults = cachedInfos
            ) }
          }
          query.length >= 2 -> {
            searchJob?.cancel()
            searchJob = searchInfos(query)
          }
        }
      }
      .launchIn(viewModelScope)
  }

  private fun searchInfos(query: String) = viewModelScope.launch {
      _state.update { it.copy(
          isLoading = true
      ) }

      infoRepository
        .getInfos()
        .onSuccess { results ->
          _state.update { it.copy(
            isLoading = false,
            errorMessage = null,
            searchResults = results
          ) }
        }
        .onError { error ->
          _state.update { it.copy(
            searchResults = emptyList(),
            isLoading = false,
            errorMessage = error.toUiText()
          ) }
        }
    }
  }