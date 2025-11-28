package com.example.community_app.ticket.presentation.ticket_master

import com.example.community_app.core.domain.location.Location
import com.example.community_app.core.presentation.helpers.UiText
import com.example.community_app.ticket.domain.Ticket
import com.example.community_app.ticket.domain.TicketDraft
import com.example.community_app.util.TicketCategory
import com.example.community_app.util.TicketStatus

data class TicketMasterState(
  val searchQuery: String = "",
  val selectedTabIndex: Int = 0,
  val filter: TicketFilterState = TicketFilterState(),
  val isFilterSheetVisible: Boolean = false,
  val communityTickets: List<Ticket> = emptyList(),
  val userTicketsAndDrafts: List<TicketUiItem> = emptyList(),
  val communitySearchResults: List<TicketUiItem> = emptyList(),
  val userSearchResults: List<TicketUiItem> = emptyList(),
  val isLoading: Boolean = false,
  val errorMessage: UiText? = null,
  val userLocation: Location? = null,
  val locationPermissionGranted: Boolean = false,
  val isUserLoggedIn: Boolean = false
)

data class TicketFilterState(
  val selectedCategories: Set<TicketCategory> = emptySet(),
  val selectedStatuses: Set<TicketStatus> = emptySet(),
  val distanceRadiusKm: Float = 50f,
  val sortBy: TicketSortOption = TicketSortOption.DATE_DESC,
  val showDrafts: Boolean = true,
  val expandedSections: Set<TicketFilterSection> = setOf(TicketFilterSection.CATEGORY)
)

sealed interface TicketUiItem {
  data class Remote(val ticket: Ticket) : TicketUiItem
  data class Local(val draft: TicketDraft) : TicketUiItem
}

enum class TicketSortOption {
  DATE_DESC, DATE_ASC, ALPHABETICAL
}

enum class TicketFilterSection {
  CATEGORY, STATUS, DISTANCE
}