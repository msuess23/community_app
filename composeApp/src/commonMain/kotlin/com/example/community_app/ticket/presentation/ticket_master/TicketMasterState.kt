package com.example.community_app.ticket.presentation.ticket_master

import com.example.community_app.core.domain.location.Location
import com.example.community_app.core.presentation.components.search.FilterSection
import com.example.community_app.core.presentation.helpers.UiText
import com.example.community_app.ticket.domain.Ticket
import com.example.community_app.ticket.domain.TicketListItem
import com.example.community_app.util.TicketCategory
import com.example.community_app.util.TicketStatus

data class TicketMasterState(
  val searchQuery: String = "",
  val selectedTabIndex: Int = 0,
  val filter: TicketFilterState = TicketFilterState(),
  val isFilterSheetVisible: Boolean = false,
  val communityTickets: List<Ticket> = emptyList(),
  val userTicketsAndDrafts: List<TicketListItem> = emptyList(),
  val communitySearchResults: List<TicketListItem> = emptyList(),
  val userSearchResults: List<TicketListItem> = emptyList(),
  val isLoading: Boolean = false,
  val errorMessage: UiText? = null
)

data class TicketFilterState(
  val selectedCategories: Set<TicketCategory> = emptySet(),
  val selectedStatuses: Set<TicketStatus> = emptySet(),
  val distanceRadiusKm: Float = 50f,
  val sortBy: TicketSortOption = TicketSortOption.DATE_DESC,
  val showDrafts: Boolean = true,
  val expandedSections: Set<FilterSection> = setOf(FilterSection.CATEGORY)
)

enum class TicketSortOption(val requiresLocation: Boolean = false) {
  DATE_DESC,
  DATE_ASC,
  ALPHABETICAL,
  FAVORITES,
  DISTANCE(requiresLocation = true)
}