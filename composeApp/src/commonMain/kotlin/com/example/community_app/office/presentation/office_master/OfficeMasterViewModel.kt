package com.example.community_app.office.presentation.office_master

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.community_app.core.domain.location.Location
import com.example.community_app.core.domain.onError
import com.example.community_app.core.domain.onSuccess
import com.example.community_app.core.domain.usecase.FetchUserLocationUseCase
import com.example.community_app.core.presentation.helpers.UiText
import com.example.community_app.core.presentation.helpers.toUiText
import com.example.community_app.core.util.GeoUtil
import com.example.community_app.office.domain.OfficeRepository
import com.example.community_app.office.domain.usecase.FilterOfficesUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OfficeMasterViewModel(
  private val officeRepository: OfficeRepository,
  private val filterOfficesUseCase: FilterOfficesUseCase,
  private val fetchUserLocationUseCase: FetchUserLocationUseCase
) : ViewModel() {

  private val _searchQuery = MutableStateFlow("")
  private val _filterState = MutableStateFlow(OfficeFilterState())
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
    officeRepository.getOffices(),
    filterInputs,
    _locationPermissionGranted,
    _uiControlState
  ) { offices, inputs, permission, uiControl ->

    var filtered = filterOfficesUseCase(
      offices = offices,
      query = inputs.query,
      distanceKm = inputs.filter.distanceRadiusKm,
      userLocation = inputs.location
    )

    filtered = when (inputs.filter.sortBy) {
      OfficeSortOption.ALPHABETICAL -> filtered.sortedBy { it.name }
      OfficeSortOption.DISTANCE -> {
        if (inputs.location != null) {
          filtered.sortedBy { office ->
            val officeLoc = Location(office.address.latitude, office.address.longitude)
            GeoUtil.calculateDistanceKm(inputs.location, officeLoc)
          }
        } else {
          filtered.sortedBy { it.name }
        }
      }
    }

    OfficeMasterState(
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
    OfficeMasterState()
  )

  init {
    updateLocationAndData(forceRefresh = false)
  }

  fun onAction(action: OfficeMasterAction) {
    when (action) {
      is OfficeMasterAction.OnSearchQueryChange -> {
        _searchQuery.value = action.query
      }
      is OfficeMasterAction.OnDistanceChange -> {
        updateFilter { it.copy(distanceRadiusKm = action.distance) }
      }
      is OfficeMasterAction.OnSortChange -> {
        updateFilter { it.copy(sortBy = action.option) }
      }
      is OfficeMasterAction.OnToggleSection -> {
        updateFilter { current ->
          val newSections = if (action.section in current.expandedSections)
            current.expandedSections - action.section
          else current.expandedSections + action.section
          current.copy(expandedSections = newSections)
        }
      }
      is OfficeMasterAction.OnToggleFilterSheet -> {
        _uiControlState.update {
          it.copy(isFilterSheetVisible = !it.isFilterSheetVisible)
        }
      }
      is OfficeMasterAction.OnRefresh -> {
        updateLocationAndData(forceRefresh = true)
      }
      is OfficeMasterAction.OnOfficeClick -> {}
    }
  }

  private fun updateFilter(update: (OfficeFilterState) -> OfficeFilterState) {
    _filterState.update(update)
  }

  private fun updateLocationAndData(forceRefresh: Boolean) {
    viewModelScope.launch {
      val locResult = fetchUserLocationUseCase()
      _locationPermissionGranted.value = locResult.permissionGranted
      if (locResult.location != null) {
        _userLocation.value = locResult.location
      }

      if (forceRefresh) refreshData() else smartSync()
    }
  }

  private fun smartSync() {
    viewModelScope.launch {
      _uiControlState.update { it.copy(isLoading = true) }
      officeRepository.syncOffices()
      _uiControlState.update { it.copy(isLoading = false) }
    }
  }

  private fun refreshData() {
    viewModelScope.launch {
      _uiControlState.update { it.copy(isLoading = true, errorMessage = null) }
      officeRepository.refreshOffices()
        .onSuccess { _uiControlState.update { it.copy(isLoading = false) } }
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
    val filter: OfficeFilterState,
    val location: Location?
  )
}