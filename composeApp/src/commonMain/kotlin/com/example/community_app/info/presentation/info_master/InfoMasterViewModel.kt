package com.example.community_app.info.presentation.info_master

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.community_app.core.domain.location.Location
import com.example.community_app.core.domain.onError
import com.example.community_app.core.domain.onSuccess
import com.example.community_app.core.domain.usecase.FetchUserLocationUseCase
import com.example.community_app.core.presentation.helpers.UiText
import com.example.community_app.core.presentation.helpers.toUiText
import com.example.community_app.info.domain.InfoRepository
import com.example.community_app.info.domain.usecase.FilterInfosUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class InfoMasterViewModel(
  private val infoRepository: InfoRepository,
  private val filterInfosUseCase: FilterInfosUseCase,
  private val fetchUserLocationUseCase: FetchUserLocationUseCase
) : ViewModel() {
  private val _searchQuery = MutableStateFlow("")
  private val _filterState = MutableStateFlow(InfoFilterState())
  private val _userLocation = MutableStateFlow<Location?>(null)
  private val _locationPermissionGranted = MutableStateFlow(false)
  private val _uiControlState = MutableStateFlow(UiControlState())

  private val filterInputs = combine(
    _searchQuery,
    _filterState,
    _userLocation
  ) { query, filter, location ->
    FilterInputs(query, filter, location)
  }

  val state = combine(
    infoRepository.getInfos(),
    filterInputs,
    _locationPermissionGranted,
    _uiControlState
  ) { infos, inputs, permission, uiControl ->
    val filtered = filterInfosUseCase(
      infos = infos,
      query = inputs.query,
      filter = inputs.filter,
      userLocation = inputs.location
    )

    InfoMasterState(
      searchQuery = inputs.query,
      filter = inputs.filter,
      isFilterSheetVisible = uiControl.isFilterSheetVisible,
      searchResults = filtered,
      isLoading = uiControl.isLoading,
      errorMessage = uiControl.errorMessage,
      userLocation = inputs.location,
      locationPermissionGranted = permission
    )
  }.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5000L),
    InfoMasterState()
  )

  init {
    checkLocationPermissionAndFetch()
    performSmartSync()
  }

  fun onAction(action: InfoMasterAction) {
    when (action) {
      is InfoMasterAction.OnSearchQueryChange -> {
        _searchQuery.value = action.query
      }

      is InfoMasterAction.OnSortChange -> {
        updateFilter { it.copy(sortBy = action.option) }
      }

      is InfoMasterAction.OnCategorySelect -> {
        updateFilter { current ->
          val newCats = if (action.category in current.selectedCategories)
            current.selectedCategories - action.category
          else current.selectedCategories + action.category
          current.copy(selectedCategories = newCats)
        }
      }

      is InfoMasterAction.OnStatusSelect -> {
        updateFilter { current ->
          val newStats = if (action.status in current.selectedStatuses)
            current.selectedStatuses - action.status
          else current.selectedStatuses + action.status
          current.copy(selectedStatuses = newStats)
        }
      }

      is InfoMasterAction.OnDistanceChange -> {
        updateFilter { it.copy(distanceRadiusKm = action.distance) }
      }

      is InfoMasterAction.OnClearCategories -> {
        updateFilter { it.copy(selectedCategories = emptySet()) }
      }

      is InfoMasterAction.OnClearStatuses -> {
        updateFilter { it.copy(selectedStatuses = emptySet()) }
      }

      is InfoMasterAction.OnToggleSection -> {
        updateFilter { current ->
          val newSections = if (action.section in current.expandedSections)
            current.expandedSections - action.section
          else current.expandedSections + action.section
          current.copy(expandedSections = newSections)
        }
      }

      is InfoMasterAction.OnToggleFilterSheet -> {
        _uiControlState.update {
          it.copy(isFilterSheetVisible = !it.isFilterSheetVisible)
        }
      }

      is InfoMasterAction.OnRefresh -> {
        checkLocationPermissionAndFetch(forceRefresh = true)
      }

      is InfoMasterAction.OnInfoClick -> {}
    }
  }

  private fun updateFilter(update: (InfoFilterState) -> InfoFilterState) {
    _filterState.update(update)
  }

  private fun checkLocationPermissionAndFetch(forceRefresh: Boolean = false) {
    viewModelScope.launch {
      val locResult = fetchUserLocationUseCase()
      _locationPermissionGranted.value = locResult.permissionGranted
      if (locResult.location != null) {
        _userLocation.value = locResult.location
      }

      if (forceRefresh) refreshData() else performSmartSync()
    }
  }

  private fun performSmartSync() {
    viewModelScope.launch {
      _uiControlState.update { it.copy(isLoading = true) }
      infoRepository.syncInfos()
      _uiControlState.update { it.copy(isLoading = false) }
    }
  }

  private fun refreshData() {
    viewModelScope.launch {
      _uiControlState.update { it.copy(isLoading = true, errorMessage = null) }

      infoRepository.refreshInfos()
        .onSuccess {
          _uiControlState.update { it.copy(isLoading = false) }
        }
        .onError { error ->
          _uiControlState.update { it.copy(isLoading = false, errorMessage = error.toUiText()) }
        }
    }
  }

  private data class UiControlState(
    val isLoading: Boolean = false,
    val errorMessage: UiText? = null,
    val isFilterSheetVisible: Boolean = false
  )

  private data class FilterInputs(
    val query: String,
    val filter: InfoFilterState,
    val location: Location?
  )
}