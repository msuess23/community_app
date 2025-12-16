package com.example.community_app.ticket.presentation.ticket_master

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.community_app.core.presentation.helpers.toUiText
import com.example.community_app.core.presentation.state.UiControlState
import com.example.community_app.ticket.domain.TicketListItem
import com.example.community_app.ticket.domain.usecase.master.FilterTicketsUseCase
import com.example.community_app.ticket.domain.usecase.master.ObserveTicketsUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class TicketMasterViewModel(
  private val observeTickets: ObserveTicketsUseCase,
  private val filterTickets: FilterTicketsUseCase
): ViewModel() {
  private val _searchQuery = MutableStateFlow("")
  private val _filterState = MutableStateFlow(TicketFilterState())
  private val _selectedTabIndex = MutableStateFlow(0)
  private val _forceRefreshTrigger = MutableStateFlow(false)
  private val _uiControlState = MutableStateFlow(UiControlState())

  private val inputs = combine(
    _searchQuery,
    _filterState,
    _selectedTabIndex,
    _forceRefreshTrigger,
    _uiControlState
  ) { query, filter, tabIndex, forceRefresh, uiControl ->
    Inputs(query, filter, tabIndex, forceRefresh, uiControl)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  val state = inputs.flatMapLatest { inputs ->
    observeTickets(forceRefresh = inputs.forceRefresh).map { result ->
      val communityTickets = result.communityTickets.map { TicketListItem.Remote(it) }

      val filteredCommunity = filterTickets(
        items = communityTickets,
        query = inputs.query,
        filter = inputs.filter,
        isUserList = false
      )

      val filteredUser = filterTickets(
        items = result.myTickets,
        query = inputs.query,
        filter = inputs.filter,
        isUserList = true
      )

      if (inputs.forceRefresh && !result.syncStatus.isLoading) {
        _forceRefreshTrigger.value = false
      }

      TicketMasterState(
        searchQuery = inputs.query,
        selectedTabIndex = inputs.tabIndex,
        filter = inputs.filter,
        communityTickets = result.communityTickets,
        userTicketsAndDrafts = result.myTickets,
        communitySearchResults = filteredCommunity,
        userSearchResults = filteredUser,
        isFilterSheetVisible = inputs.uiControl.isFilterSheetVisible,
        isLoading = result.syncStatus.isLoading,
        errorMessage = result.syncStatus.error?.toUiText()
      )
    }
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
      }

      else -> Unit
    }
  }

  private fun updateFilter(update: (TicketFilterState) -> TicketFilterState) {
    _filterState.update(update)
  }

  private data class Inputs(
    val query: String,
    val filter: TicketFilterState,
    val tabIndex: Int,
    val forceRefresh: Boolean,
    val uiControl: UiControlState
  )
}