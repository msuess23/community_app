package com.example.community_app.ticket.presentation.ticket_master

import com.example.community_app.core.presentation.components.search.FilterSection
import com.example.community_app.ticket.domain.model.Ticket
import com.example.community_app.ticket.domain.model.TicketDraft
import com.example.community_app.util.TicketCategory
import com.example.community_app.util.TicketStatus

sealed interface TicketMasterAction {
  data class OnSearchQueryChange(val query: String): TicketMasterAction
  data class OnTabChange(val index: Int) : TicketMasterAction
  data object OnLoginClick : TicketMasterAction
  data object OnCreateTicketClick : TicketMasterAction
  data class OnTicketClick(val ticket: Ticket) : TicketMasterAction
  data class OnDraftClick(val draft: TicketDraft) : TicketMasterAction
  data object OnRefresh : TicketMasterAction
  data object OnToggleFilterSheet : TicketMasterAction
  data class OnSortChange(val option: TicketSortOption) : TicketMasterAction
  data class OnCategorySelect(val category: TicketCategory) : TicketMasterAction
  data object OnClearCategories : TicketMasterAction
  data class OnStatusSelect(val status: TicketStatus) : TicketMasterAction
  data object OnClearStatuses : TicketMasterAction
  data class OnDistanceChange(val distance: Float) : TicketMasterAction
  data class OnToggleShowDrafts(val show: Boolean) : TicketMasterAction
  data class OnToggleSection(val section: FilterSection) : TicketMasterAction
}