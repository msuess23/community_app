package com.example.community_app.office.presentation.office_master

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.community_app.core.presentation.helpers.toUiText
import com.example.community_app.office.domain.usecase.FilterOfficesUseCase
import com.example.community_app.office.domain.usecase.ObserveOfficesUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class OfficeMasterViewModel(
  private val observeOffices: ObserveOfficesUseCase,
  private val filterOffices: FilterOfficesUseCase
) : ViewModel() {

  private val _searchQuery = MutableStateFlow("")
  private val _filterState = MutableStateFlow(OfficeFilterState())
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
    observeOffices(forceRefresh = inputs.forceRefresh).map { result ->
      val filteredOffices = filterOffices(
        offices = result.offices,
        query = inputs.query,
        filter = inputs.filter
      )

      if (inputs.forceRefresh && !result.isLoading) {
        _forceRefreshTrigger.value = false
      }

      OfficeMasterState(
        searchQuery = inputs.query,
        filter = inputs.filter,
        isFilterSheetVisible = inputs.isFilterVisible,
        searchResults = filteredOffices,
        isLoading = result.isLoading,
        errorMessage = result.error?.toUiText()
      )
    }
  }.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5000L),
    OfficeMasterState()
  )

  fun onAction(action: OfficeMasterAction) {
    when (action) {
      is OfficeMasterAction.OnSearchQueryChange -> {
        _searchQuery.value = action.query
      }
      is OfficeMasterAction.OnDistanceChange -> updateFilter {
        it.copy(distanceRadiusKm = action.distance)
      }
      is OfficeMasterAction.OnSortChange -> updateFilter {
        it.copy(sortBy = action.option)
      }
      is OfficeMasterAction.OnToggleSection -> updateFilter { current ->
        val newSections = if (action.section in current.expandedSections) {
          current.expandedSections - action.section
        } else {
          current.expandedSections + action.section
        }
        current.copy(expandedSections = newSections)
      }
      is OfficeMasterAction.OnToggleFilterSheet -> {
        _isFilterSheetVisible.update { !it }
      }
      is OfficeMasterAction.OnRefresh -> {
        _forceRefreshTrigger.value = true
      }
      else -> Unit
    }
  }

  private fun updateFilter(update: (OfficeFilterState) -> OfficeFilterState) {
    _filterState.update(update)
  }

  private data class Inputs(
    val query: String,
    val filter: OfficeFilterState,
    val forceRefresh: Boolean,
    val isFilterVisible: Boolean
  )
}