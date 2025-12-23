package com.example.community_app.office.presentation.office_master

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.community_app.core.domain.location.Location
import com.example.community_app.core.domain.usecase.FetchUserLocationUseCase
import com.example.community_app.core.presentation.helpers.toUiText
import com.example.community_app.office.domain.usecase.FilterOfficesUseCase
import com.example.community_app.office.domain.usecase.ObserveOfficesUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OfficeMasterViewModel(
  private val observeOffices: ObserveOfficesUseCase,
  private val filterOffices: FilterOfficesUseCase,
  private val fetchUserLocation: FetchUserLocationUseCase
) : ViewModel() {
  private val _searchQuery = MutableStateFlow("")
  private val _filterState = MutableStateFlow(OfficeFilterState())
  private val _forceRefreshTrigger = MutableStateFlow(false)
  private val _isFilterSheetVisible = MutableStateFlow(false)

  private val _userLocation = MutableStateFlow<Location?>(null)

  init { refreshLocation() }

  @OptIn(ExperimentalCoroutinesApi::class)
  private val officeDataFlow = _forceRefreshTrigger.flatMapLatest { force ->
    observeOffices(forceRefresh = force)
  }

  val state = combine(
    officeDataFlow,
    _searchQuery,
    _filterState,
    _isFilterSheetVisible,
    _userLocation
  ) { dataResult, query, filter, isFilterVisible, location ->
    if (_forceRefreshTrigger.value && !dataResult.isLoading) {
      _forceRefreshTrigger.value = false
    }

    val filteredOffices = filterOffices(
      offices = dataResult.offices,
      query = query,
      filter = filter,
      userLocation = location
    )

    OfficeMasterState(
      searchQuery = query,
      filter = filter,
      isFilterSheetVisible = isFilterVisible,
      searchResults = filteredOffices,
      isLoading = dataResult.isLoading,
      errorMessage = dataResult.error?.toUiText()
    )
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
        refreshLocation()
      }
      else -> Unit
    }
  }

  private fun updateFilter(update: (OfficeFilterState) -> OfficeFilterState) {
    _filterState.update(update)
  }

  private fun refreshLocation() {
    viewModelScope.launch {
      try {
        val result = fetchUserLocation()
        if (result.location != null) {
          _userLocation.value = result.location
        }
      } catch (_: Exception) { }
    }
  }
}