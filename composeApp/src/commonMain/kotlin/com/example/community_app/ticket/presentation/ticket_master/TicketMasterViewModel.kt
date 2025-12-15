package com.example.community_app.ticket.presentation.ticket_master

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.community_app.core.domain.location.Location
import com.example.community_app.core.domain.onError
import com.example.community_app.core.domain.onSuccess
import com.example.community_app.core.domain.usecase.FetchUserLocationUseCase
import com.example.community_app.core.presentation.helpers.UiText
import com.example.community_app.core.presentation.helpers.toUiText
import com.example.community_app.ticket.domain.TicketListItem
import com.example.community_app.ticket.domain.TicketRepository
import com.example.community_app.ticket.domain.usecase.master.FilterTicketsUseCase
import com.example.community_app.ticket.domain.usecase.master.ObserveCommunityTicketsUseCase
import com.example.community_app.ticket.domain.usecase.master.ObserveMyTicketsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TicketMasterViewModel(
  private val ticketRepository: TicketRepository,
  private val filterTickets: FilterTicketsUseCase,
  observeCommunityTickets: ObserveCommunityTicketsUseCase,
  observeMyTickets: ObserveMyTicketsUseCase,
  private val fetchUserLocation: FetchUserLocationUseCase
): ViewModel() {
  private val _searchQuery = MutableStateFlow("")
  private val _filterState = MutableStateFlow(TicketFilterState())
  private val _userLocation = MutableStateFlow<Location?>(null)
  private val _locationPermissionGranted = MutableStateFlow(false)
  private val _selectedTabIndex = MutableStateFlow(0)
  private val _uiControlState = MutableStateFlow(UiControlState())

  private val communityTicketsRaw = observeCommunityTickets()
    .map { list -> list.map { TicketListItem.Remote(it) } }

  private val userTicketsRaw = observeMyTickets()

  private val filterInputs = combine(
    _searchQuery,
    _filterState,
    _userLocation,
    _selectedTabIndex
  ) { query, filter, loc, tabIndex ->
    FilterInputs(query, filter, loc, tabIndex)
  }

  val state = combine(
    communityTicketsRaw,
    userTicketsRaw,
    filterInputs,
    _uiControlState
  ) { communityRaw, userRaw, inputs, uiControl ->
    val filteredCommunity = filterTickets(
      items = communityRaw,
      query = inputs.query,
      filter = inputs.filter,
      userLocation = inputs.location,
      isUserList = false
    )

    val filteredUser = filterTickets(
      items = userRaw,
      query = inputs.query,
      filter = inputs.filter,
      userLocation = inputs.location,
      isUserList = true
    )

    TicketMasterState(
      searchQuery = inputs.query,
      selectedTabIndex = inputs.tabIndex,
      filter = inputs.filter,
      communityTickets = communityRaw.map { it.ticket },
      userTicketsAndDrafts = userRaw,
      communitySearchResults = filteredCommunity,
      userSearchResults = filteredUser,
      isFilterSheetVisible = uiControl.isFilterSheetVisible,
      isLoading = uiControl.isLoading,
      errorMessage = uiControl.errorMessage,
      userLocation = inputs.location,
      locationPermissionGranted = _locationPermissionGranted.value
    )
  }.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5000L),
    TicketMasterState()
  )

  init {
    updateLocationAndData(forceRefresh = false)
  }

  fun onAction(action: TicketMasterAction) {
    when (action) {
      is TicketMasterAction.OnSearchQueryChange -> {
        _searchQuery.value = action.query
      }
      is TicketMasterAction.OnTabChange -> {
        _selectedTabIndex.value = action.index
      }

      is TicketMasterAction.OnSortChange -> {
        updateFilter { it.copy(sortBy = action.option) }
      }
      is TicketMasterAction.OnCategorySelect -> {
        updateFilter { current ->
          val newCats = if (action.category in current.selectedCategories) {
            current.selectedCategories - action.category
          }
          else {
            current.selectedCategories + action.category
          }
          current.copy(selectedCategories = newCats)
        }
      }

      is TicketMasterAction.OnStatusSelect -> {
        updateFilter { current ->
          val newStats = if (action.status in current.selectedStatuses) {
            current.selectedStatuses - action.status
          }
          else {
            current.selectedStatuses + action.status
          }
          current.copy(selectedStatuses = newStats)
        }
      }

      is TicketMasterAction.OnDistanceChange -> {
        updateFilter { it.copy(distanceRadiusKm = action.distance) }
      }
      is TicketMasterAction.OnToggleShowDrafts -> {
        updateFilter { it.copy(showDrafts = action.show) }
      }
      TicketMasterAction.OnClearCategories -> {
        updateFilter { it.copy(selectedCategories = emptySet()) }
      }
      TicketMasterAction.OnClearStatuses -> {
        updateFilter { it.copy(selectedStatuses = emptySet()) }
      }
      is TicketMasterAction.OnToggleSection -> {
        updateFilter { current ->
          val newSections = if (action.section in current.expandedSections) {
            current.expandedSections - action.section
          }
          else {
            current.expandedSections + action.section
          }
          current.copy(expandedSections = newSections)
        }
      }

      TicketMasterAction.OnToggleFilterSheet -> {
        _uiControlState.update {
          it.copy(isFilterSheetVisible = !it.isFilterSheetVisible)
        }
      }

      TicketMasterAction.OnRefresh -> {
        updateLocationAndData(forceRefresh = true)
      }

      else -> Unit
    }
  }

  private fun updateFilter(update: (TicketFilterState) -> TicketFilterState) {
    _filterState.update(update)
  }

  private fun updateLocationAndData(forceRefresh: Boolean) {
    viewModelScope.launch {
      val locResult = fetchUserLocation()
      _locationPermissionGranted.value = locResult.permissionGranted
      if (locResult.location != null) {
        _userLocation.value = locResult.location
      }

      if (forceRefresh) {
        refreshData()
      } else {
        smartSync()
      }
    }
  }

  private fun refreshData() {
    viewModelScope.launch {
      _uiControlState.update { it.copy(
        isLoading = true,
        errorMessage = null)
      }

      ticketRepository.refreshTickets()
        .onSuccess {
          _uiControlState.update { it.copy(isLoading = false) }
        }
        .onError { error ->
          _uiControlState.update { it.copy(
            isLoading = false,
            errorMessage = error.toUiText())
          }
        }
    }
  }

  private fun smartSync() {
    viewModelScope.launch {
      _uiControlState.update { it.copy(isLoading = true) }
      ticketRepository.syncTickets()
      _uiControlState.update { it.copy(isLoading = false) }
    }
  }

  private data class UiControlState(
    val isLoading: Boolean = false,
    val errorMessage: UiText? = null,
    val isFilterSheetVisible: Boolean = false
  )

  private data class FilterInputs(
    val query: String,
    val filter: TicketFilterState,
    val location: Location?,
    val tabIndex: Int
  )
}