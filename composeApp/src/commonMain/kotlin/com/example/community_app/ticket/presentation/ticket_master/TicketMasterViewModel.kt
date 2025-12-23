package com.example.community_app.ticket.presentation.ticket_master

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.community_app.core.domain.location.Location
import com.example.community_app.core.domain.usecase.FetchUserLocationUseCase
import com.example.community_app.core.presentation.helpers.toUiText
import com.example.community_app.core.presentation.state.UiControlState
import com.example.community_app.ticket.domain.model.TicketListItem
import com.example.community_app.ticket.domain.usecase.master.FilterTicketsUseCase
import com.example.community_app.ticket.domain.usecase.master.ObserveTicketsUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TicketMasterViewModel(
  private val observeTickets: ObserveTicketsUseCase,
  private val filterTickets: FilterTicketsUseCase,
  private val fetchUserLocation: FetchUserLocationUseCase
): ViewModel() {
  private val _searchQuery = MutableStateFlow("")
  private val _filterState = MutableStateFlow(TicketFilterState())
  private val _selectedTabIndex = MutableStateFlow(0)
  private val _forceRefreshTrigger = MutableStateFlow(false)
  private val _uiControlState = MutableStateFlow(UiControlState())

  private val _userLocation = MutableStateFlow<Location?>(null)

  init { refreshLocation() }

  @OptIn(ExperimentalCoroutinesApi::class)
  private val ticketDataFlow = _forceRefreshTrigger.flatMapLatest { force ->
    observeTickets(forceRefresh = force)
  }

  private val filterInputsFlow = combine(
    _searchQuery,
    _filterState,
    _selectedTabIndex,
    _userLocation
  ) { query, filter, tabIndex, location ->
    FilterInputs(query, filter, tabIndex, location)
  }

  val state = combine(
    ticketDataFlow,
    filterInputsFlow,
    _uiControlState,
    _forceRefreshTrigger
  ) { dataResult, inputs, uiState, forceRefresh ->
    val (query, filter, tabIndex, location) = inputs

    if (forceRefresh && !dataResult.syncStatus.isLoading) {
      _forceRefreshTrigger.value = false
    }

    val filteredCommunity = filterTickets(
      items = dataResult.communityTickets.map { TicketListItem.Remote(it) },
      query = inputs.query,
      filter = inputs.filter,
      isUserList = false,
      userLocation = location
    )

    val filteredUser = filterTickets(
      items = dataResult.myTickets,
      query = inputs.query,
      filter = inputs.filter,
      isUserList = true,
      userLocation = location
    )

    TicketMasterState(
      searchQuery = query,
      selectedTabIndex = tabIndex,
      filter = filter,
      communityTickets = dataResult.communityTickets,
      userTicketsAndDrafts = dataResult.myTickets,
      communitySearchResults = filteredCommunity,
      userSearchResults = filteredUser,
      isFilterSheetVisible = uiState.isFilterSheetVisible,
      isLoading = dataResult.syncStatus.isLoading,
      errorMessage = dataResult.syncStatus.error?.toUiText()
    )
  }.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5000L),
    TicketMasterState(isLoading = true)
  )

  fun onAction(action: TicketMasterAction) {
    when (action) {
      is TicketMasterAction.OnSearchQueryChange -> {
        _searchQuery.value = action.query
      }
      is TicketMasterAction.OnTabChange -> {
        _selectedTabIndex.value = action.index
      }

      is TicketMasterAction.OnSortChange -> updateFilter {
        it.copy(sortBy = action.option)
      }
      is TicketMasterAction.OnCategorySelect -> updateFilter { current ->
        val newCats = if (action.category in current.selectedCategories) {
          current.selectedCategories - action.category
        } else {
          current.selectedCategories + action.category
        }
        current.copy(selectedCategories = newCats)
      }

      is TicketMasterAction.OnStatusSelect -> updateFilter { current ->
        val newStats = if (action.status in current.selectedStatuses) {
          current.selectedStatuses - action.status
        } else {
          current.selectedStatuses + action.status
        }
        current.copy(selectedStatuses = newStats)
      }


      is TicketMasterAction.OnDistanceChange -> updateFilter {
        it.copy(distanceRadiusKm = action.distance)
      }
      is TicketMasterAction.OnToggleShowDrafts -> updateFilter {
        it.copy(showDrafts = action.show)
      }
      TicketMasterAction.OnClearCategories -> updateFilter {
        it.copy(selectedCategories = emptySet())
      }
      TicketMasterAction.OnClearStatuses -> updateFilter {
        it.copy(selectedStatuses = emptySet())
      }
      is TicketMasterAction.OnToggleSection -> updateFilter { current ->
        val newSections = if (action.section in current.expandedSections) {
          current.expandedSections - action.section
        } else {
          current.expandedSections + action.section
        }
        current.copy(expandedSections = newSections)
      }

      TicketMasterAction.OnToggleFilterSheet -> {
        _uiControlState.update {
          it.copy(isFilterSheetVisible = !it.isFilterSheetVisible)
        }
      }

      TicketMasterAction.OnRefresh -> {
        _forceRefreshTrigger.value = true
        refreshLocation()
      }

      else -> Unit
    }
  }

  private fun updateFilter(update: (TicketFilterState) -> TicketFilterState) {
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

  private data class FilterInputs(
    val query: String,
    val filter: TicketFilterState,
    val selectedTab: Int,
    val userLocation: Location?
  )
}