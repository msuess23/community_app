package com.example.community_app.info.presentation.info_master

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.community_app.core.domain.onError
import com.example.community_app.core.domain.onSuccess
import com.example.community_app.core.presentation.helpers.toUiText
import com.example.community_app.info.domain.Info
import com.example.community_app.info.domain.InfoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InfoMasterViewModel(
  private val infoRepository: InfoRepository
): ViewModel() {
  private val _state = MutableStateFlow(InfoMasterState())
  private val _allInfos = MutableStateFlow<List<Info>>(emptyList())

  val state = _state
    .onStart {
      observeDb()
      performSmartSync()
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
        _state.update { it.copy(
          searchQuery = action.query
        ) }
        applyFilters()
      }

      is InfoMasterAction.OnRefresh -> {
        refreshData()
      }

      is InfoMasterAction.OnToggleFilterSheet -> {
        _state.update { it.copy(
          isFilterSheetVisible = !it.isFilterSheetVisible
        ) }
      }

      is InfoMasterAction.OnCategorySelect -> {
        _state.update { currentState ->
          val currentCategories = currentState.filter.selectedCategories
          val newCategories = if (currentCategories.contains(action.category)) {
            currentCategories - action.category
          } else {
            currentCategories + action.category
          }
          currentState.copy(
            filter = currentState.filter.copy(
              selectedCategories = newCategories
            )
          )
        }
        applyFilters()
      }

      is InfoMasterAction.OnClearCategories -> {
        _state.update { it.copy(
          filter = it.filter.copy(
            selectedCategories = emptySet()
          )
        ) }
        applyFilters()
      }

      is InfoMasterAction.OnSortChange -> {
        _state.update { it.copy(
          filter = it.filter.copy(
            sortBy = action.option
          )
        ) }
        applyFilters()
      }
    }
  }

  private fun observeDb() {
    infoRepository.getInfos()
      .onEach { infos ->
        _allInfos.value = infos
        applyFilters()
      }
      .launchIn(viewModelScope)
  }

  private fun applyFilters() {
    val query = _state.value.searchQuery
    val filters = _state.value.filter
    val rawInfos = _allInfos.value

    var result = rawInfos

    if (query.isNotBlank()) {
      result = result.filter {
        it.title.contains(query, ignoreCase = true) ||
        (it.description?.contains(query, ignoreCase = true) == true)
      }
    }

    if (filters.selectedCategories.isNotEmpty()) {
      result = result.filter {
        it.category in filters.selectedCategories
      }
    }

    result = when(filters.sortBy) {
      InfoSortOption.DATE_DESC -> result.sortedByDescending { it.startsAt }
      InfoSortOption.DATE_ASC -> result.sortedBy { it.startsAt }
      InfoSortOption.ALPHABETICAL -> result.sortedBy { it.title }
    }

    _state.update { it.copy(searchResults = result) }
  }

  private fun performSmartSync() {
    viewModelScope.launch {
      if (_allInfos.value.isEmpty()) {
        _state.update { it.copy(isLoading = true) }
      }

      infoRepository.syncInfos()
      _state.update { it.copy(isLoading = false) }
    }
  }

  private fun refreshData() {
    viewModelScope.launch {
      _state.update { it.copy(isLoading = true) }

      infoRepository.refreshInfos()
        .onSuccess {
          _state.update { it.copy(
            isLoading = false,
            errorMessage = null
          ) }
        }
        .onError { error ->
          _state.update { it.copy(
            isLoading = false,
            errorMessage = error.toUiText()
          ) }
        }
    }
  }
}