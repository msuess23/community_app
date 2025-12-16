package com.example.community_app.info.presentation.info_master

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.community_app.core.presentation.helpers.toUiText
import com.example.community_app.info.domain.usecase.FilterInfosUseCase
import com.example.community_app.info.domain.usecase.ObserveInfosUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class InfoMasterViewModel(
  private val observeInfos: ObserveInfosUseCase,
  private val filterInfos: FilterInfosUseCase
) : ViewModel() {
  private val _searchQuery = MutableStateFlow("")
  private val _filterState = MutableStateFlow(InfoFilterState())
  private val _forceRefreshTrigger = MutableStateFlow(false)
  private val _isFilterSheetVisible = MutableStateFlow(false)

  private val inputs = combine(
    _searchQuery,
    _filterState,
    _forceRefreshTrigger,
    _isFilterSheetVisible
  ) { query, filter, forceRefresh, isFilterVisible ->
    Inputs(query, filter, forceRefresh, isFilterVisible)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  val state = inputs.flatMapLatest { inputs ->
    observeInfos(forceRefresh = inputs.forceRefresh).map { result ->
      val filteredInfos = filterInfos(
        infos = result.infos,
        query = inputs.query,
        filter = inputs.filter
      )

      if (inputs.forceRefresh && !result.isLoading) {
        _forceRefreshTrigger.value = false
      }

      InfoMasterState(
        searchQuery = inputs.query,
        filter = inputs.filter,
        isFilterSheetVisible = inputs.isFilterVisible,
        searchResults = filteredInfos,
        isLoading = result.isLoading,
        errorMessage = result.error?.toUiText()
      )
    }
  }.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5000L),
    InfoMasterState(isLoading = true)
  )

  fun onAction(action: InfoMasterAction) {
    when (action) {
      is InfoMasterAction.OnSearchQueryChange -> {
        _searchQuery.value = action.query
      }

      is InfoMasterAction.OnSortChange -> {
        updateFilter { it.copy(sortBy = action.option) }
      }

      is InfoMasterAction.OnCategorySelect -> { updateFilter { current ->
        val newCats = if (action.category in current.selectedCategories) {
          current.selectedCategories - action.category
        } else {
          current.selectedCategories + action.category
        }
        current.copy(selectedCategories = newCats)
      } }

      is InfoMasterAction.OnStatusSelect -> { updateFilter { current ->
        val newStats = if (action.status in current.selectedStatuses) {
          current.selectedStatuses - action.status
        } else {
          current.selectedStatuses + action.status
        }
        current.copy(selectedStatuses = newStats)
      } }

      is InfoMasterAction.OnDistanceChange -> {
        updateFilter { it.copy(distanceRadiusKm = action.distance) }
      }

      is InfoMasterAction.OnClearCategories -> {
        updateFilter { it.copy(selectedCategories = emptySet()) }
      }

      is InfoMasterAction.OnClearStatuses -> {
        updateFilter { it.copy(selectedStatuses = emptySet()) }
      }

      is InfoMasterAction.OnToggleSection -> { updateFilter { current ->
        val newSections = if (action.section in current.expandedSections) {
          current.expandedSections - action.section
        } else {
          current.expandedSections + action.section
        }
        current.copy(expandedSections = newSections)
      } }

      is InfoMasterAction.OnToggleFilterSheet -> {
        _isFilterSheetVisible.update { !it }
      }

      is InfoMasterAction.OnRefresh -> {
        _forceRefreshTrigger.value = true
      }

      is InfoMasterAction.OnInfoClick -> {}
    }
  }

  private fun updateFilter(update: (InfoFilterState) -> InfoFilterState) {
    _filterState.update(update)
  }

  private data class Inputs(
    val query: String,
    val filter: InfoFilterState,
    val forceRefresh: Boolean,
    val isFilterVisible: Boolean
  )
}