package com.example.community_app.ticket.presentation.ticket_master

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.community_app.auth.domain.AuthRepository
import com.example.community_app.auth.domain.AuthState
import com.example.community_app.core.domain.location.Location
import com.example.community_app.core.domain.location.LocationService
import com.example.community_app.core.domain.onError
import com.example.community_app.core.domain.onSuccess
import com.example.community_app.core.domain.permission.AppPermissionService
import com.example.community_app.core.presentation.helpers.toUiText
import com.example.community_app.core.util.GeoUtil
import com.example.community_app.ticket.domain.TicketRepository
import com.example.community_app.ticket.presentation.ticket_master.TicketMasterAction
import com.example.community_app.ticket.presentation.ticket_master.TicketMasterState
import com.example.community_app.ticket.presentation.ticket_master.TicketSortOption
import com.example.community_app.ticket.presentation.ticket_master.TicketUiItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TicketMasterViewModel(
  private val ticketRepository: TicketRepository,
  private val authRepository: AuthRepository,
  private val locationService: LocationService,
  private val permissionService: AppPermissionService
): ViewModel() {
  private val _state = MutableStateFlow(TicketMasterState())

  val state = _state
    .onStart {
      observeAuth()
      checkLocationPermissionAndFetch()
    }
    .stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000L),
      _state.value
    )

  fun onAction(action: TicketMasterAction) {
    when(action) {
      is TicketMasterAction.OnSearchQueryChange -> {
        _state.update { it.copy(searchQuery = action.query) }
        applyFilters()
      }

      is TicketMasterAction.OnTabChange -> {
        _state.update { it.copy(selectedTabIndex = action.index) }
      }

      is TicketMasterAction.OnRefresh -> {
        checkLocationPermissionAndFetch(forceRefresh = true)
      }

      is TicketMasterAction.OnToggleFilterSheet -> {
        _state.update { it.copy(isFilterSheetVisible = !it.isFilterSheetVisible) }
      }

      is TicketMasterAction.OnSortChange -> {
        _state.update { it.copy(
          filter = it.filter.copy(sortBy = action.option)
        ) }
        applyFilters()
      }

      is TicketMasterAction.OnCategorySelect -> {
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

      is TicketMasterAction.OnClearCategories -> {
        _state.update { it.copy(
          filter = it.filter.copy(
            selectedCategories = emptySet()
          )
        ) }
        applyFilters()
      }

      is TicketMasterAction.OnStatusSelect -> {
        _state.update { currentState ->
          val currentStats = currentState.filter.selectedStatuses
          val newStats = if (currentStats.contains(action.status)) {
            currentStats - action.status
          } else {
            currentStats + action.status
          }
          currentState.copy(
            filter = currentState.filter.copy(selectedStatuses = newStats)
          )
        }
        applyFilters()
      }

      is TicketMasterAction.OnClearStatuses -> {
        _state.update { it.copy(filter = it.filter.copy(selectedStatuses = emptySet())) }
        applyFilters()
      }

      is TicketMasterAction.OnDistanceChange -> {
        _state.update { it.copy(filter = it.filter.copy(distanceRadiusKm = action.distance)) }
        applyFilters()
      }

      is TicketMasterAction.OnToggleShowDrafts -> {
        _state.update { it.copy(filter = it.filter.copy(showDrafts = action.show)) }
        applyFilters()
      }

      is TicketMasterAction.OnToggleSection -> {
        _state.update { currentState ->
          val sections = currentState.filter.expandedSections
          val newSections = if (sections.contains(action.section)) {
            sections - action.section
          } else {
            sections + action.section
          }
          currentState.copy(filter = currentState.filter.copy(expandedSections = newSections))
        }
      }

      else -> Unit
    }
  }

  private fun observeAuth() {
    authRepository.authState.onEach { authState ->
      if (authState is AuthState.Authenticated) {
        _state.update { it.copy(isUserLoggedIn = true) }
        observeTickets(authState.user.id)
      } else {
        _state.update { it.copy(isUserLoggedIn = false) }
        observeTickets(null)
      }
    }.launchIn(viewModelScope)
  }

  private fun observeTickets(userId: Int? = null) {
    // Community tickets
    val communityFlow = if (userId != null) {
      ticketRepository.getCommunityTickets(userId)
    } else {
      ticketRepository.getTickets()
    }

    communityFlow.onEach { tickets ->
      _state.update { it.copy(communityTickets = tickets) }
      applyFilters()
    }.launchIn(viewModelScope)

    // User tickets and drafts
    if (userId != null) {
      combine(
        ticketRepository.getUserTickets(userId),
        ticketRepository.getDrafts()
      ) { tickets, drafts ->
        val combined = mutableListOf<TicketUiItem>()
        combined.addAll(drafts.map { TicketUiItem.Local(it) })
        combined.addAll(tickets.map { TicketUiItem.Remote(it) })
        combined.toList()
      }
        .onEach { items ->
          _state.update { it.copy(userTicketsAndDrafts = items) }
          applyFilters()
        }.launchIn(viewModelScope)
    } else {
      _state.update { it.copy(userTicketsAndDrafts = emptyList()) }
      applyFilters()
    }
  }

  private fun checkLocationPermissionAndFetch(forceRefresh: Boolean = false) {
    viewModelScope.launch {
      val hasPermission = permissionService.requestLocationPermission()
      _state.update { it.copy(isPermissionGranted = hasPermission) }

      if (hasPermission) {
        val location = locationService.getCurrentLocation()
        if (location != null) {
          _state.update { it.copy(userLocation = location) }
          applyFilters()
        }
      }

      if (forceRefresh) {
        refreshData()
      } else {
        performSmartSync()
      }
    }
  }

  private fun applyFilters() {
    val currentState = _state.value

    val communitySource = currentState.communityTickets.map { TicketUiItem.Remote(it) }
    val filteredCommunity = filterList(communitySource, isUserList = false)

    val userSource = currentState.userTicketsAndDrafts
    val filteredUser = filterList(userSource, isUserList = true)

    _state.update { it.copy(
      communitySearchResults = filteredCommunity,
      userSearchResults = filteredUser
    ) }
  }

  private fun filterList(
    source: List<TicketUiItem>,
    isUserList: Boolean
  ): List<TicketUiItem> {
    val query = _state.value.searchQuery
    val filter = _state.value.filter
    val userLocation = _state.value.userLocation

    var result = source

    if (query.isNotBlank()) {
      result = result.filter { item ->
        val (title, description) = when(item) {
          is TicketUiItem.Remote -> item.ticket.title to item.ticket.description
          is TicketUiItem.Local -> item.draft.title to item.draft.description
        }
        title.contains(query, ignoreCase = true) ||
            (description?.contains(query, ignoreCase = true) == true)
      }
    }

    if (filter.selectedCategories.isNotEmpty()) {
      result = result.filter { item ->
        val category = when(item) {
          is TicketUiItem.Remote -> item.ticket.category
          is TicketUiItem.Local -> item.draft.category
        }
        category != null && category in filter.selectedCategories
      }
    }

    if (filter.selectedStatuses.isNotEmpty()) {
      result = result.filter { item ->
        if (item is TicketUiItem.Remote) {
          val status = item.ticket.currentStatus
          status != null && status in filter.selectedStatuses
        } else {
          true
        }
      }
    }

    if (!isUserList && userLocation != null) {
      result = result.filter { item ->
        val address = when(item) {
          is TicketUiItem.Remote -> item.ticket.address
          is TicketUiItem.Local -> item.draft.address
        }
        if (address != null) {
          val itemLoc = Location(address.latitude, address.longitude)
          val dist = GeoUtil.calculateDistanceKm(userLocation, itemLoc)
          dist <= filter.distanceRadiusKm
        } else true
      }
    }

    if (isUserList && !filter.showDrafts) {
      result = result.filter { it !is TicketUiItem.Local }
    }

    return when(filter.sortBy) {
      TicketSortOption.DATE_DESC -> result.sortedByDescending {
        when(it) {
          is TicketUiItem.Remote -> it.ticket.createdAt
          is TicketUiItem.Local -> it.draft.lastModified
        }
      }
      TicketSortOption.DATE_ASC -> result.sortedBy {
        when(it) {
          is TicketUiItem.Remote -> it.ticket.createdAt
          is TicketUiItem.Local -> it.draft.lastModified
        }
      }
      TicketSortOption.ALPHABETICAL -> result.sortedBy {
        when(it) {
          is TicketUiItem.Remote -> it.ticket.title
          is TicketUiItem.Local -> it.draft.title
        }
      }
    }
  }

  private fun performSmartSync() {
    viewModelScope.launch {
      if (_state.value.communityTickets.isEmpty()) {
        _state.update { it.copy(isLoading = true) }
      }
      ticketRepository.syncTickets()
      _state.update { it.copy(isLoading = false) }
    }
  }

  private fun refreshData() {
    viewModelScope.launch {
      _state.update { it.copy(isLoading = true) }
      ticketRepository.refreshTickets()
        .onSuccess {
          _state.update { it.copy(isLoading = false) }
        }
        .onError { error ->
          _state.update {
            it.copy(isLoading = false, errorMessage = error.toUiText())
          }
        }
    }
  }
}