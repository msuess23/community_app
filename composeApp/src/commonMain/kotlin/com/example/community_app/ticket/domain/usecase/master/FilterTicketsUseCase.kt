package com.example.community_app.ticket.domain.usecase.master

import com.example.community_app.core.domain.location.Location
import com.example.community_app.core.domain.usecase.FetchUserLocationUseCase
import com.example.community_app.core.util.GeoUtil
import com.example.community_app.ticket.domain.TicketListItem
import com.example.community_app.ticket.presentation.ticket_master.TicketFilterState
import com.example.community_app.ticket.presentation.ticket_master.TicketSortOption

class FilterTicketsUseCase(
  private val fetchUserLocation: FetchUserLocationUseCase
) {
  suspend operator fun invoke(
    items: List<TicketListItem>,
    query: String,
    filter: TicketFilterState,
    isUserList: Boolean
  ): List<TicketListItem> {
    var result = items

    if (query.isNotBlank()) {
      result = result.filter { item ->
        val (title, description) = when(item) {
          is TicketListItem.Remote -> item.ticket.title to item.ticket.description
          is TicketListItem.Local -> item.draft.title to item.draft.description
        }
        title.contains(query, ignoreCase = true) ||
            (description?.contains(query, ignoreCase = true) == true)
      }
    }

    if (filter.selectedCategories.isNotEmpty()) {
      result = result.filter { item ->
        val category = when(item) {
          is TicketListItem.Remote -> item.ticket.category
          is TicketListItem.Local -> item.draft.category
        }
        category != null && category in filter.selectedCategories
      }
    }

    if (filter.selectedStatuses.isNotEmpty()) {
      result = result.filter { item ->
        if (item is TicketListItem.Remote) {
          val status = item.ticket.currentStatus
          status != null && status in filter.selectedStatuses
        } else {
          true
        }
      }
    }

    val userLocation = fetchUserLocation().location
    if (!isUserList && userLocation != null) {
      result = result.filter { item ->
        val address = when(item) {
          is TicketListItem.Remote -> item.ticket.address
          is TicketListItem.Local -> item.draft.address
        }
        if (address != null) {
          val itemLoc = Location(address.latitude, address.longitude)
          val dist = GeoUtil.calculateDistanceKm(userLocation, itemLoc)
          dist <= filter.distanceRadiusKm
        } else true
      }
    }

    if (isUserList && !filter.showDrafts) {
      result = result.filter { it !is TicketListItem.Local }
    }

    result = when (filter.sortBy) {
      TicketSortOption.DATE_DESC -> result.sortedByDescending { getSortDate(it) }
      TicketSortOption.DATE_ASC -> result.sortedBy { getSortDate(it) }
      TicketSortOption.ALPHABETICAL -> result.sortedBy { getTitle(it) }
      TicketSortOption.FAVORITES -> result.sortedByDescending { item ->
        (item as? TicketListItem.Remote)?.ticket?.isFavorite == true
      }
      TicketSortOption.DISTANCE -> {
        if (userLocation != null) {
          result.sortedBy { item ->
            val address = when(item) {
              is TicketListItem.Remote -> item.ticket.address
              is TicketListItem.Local -> item.draft.address
            }

            if (address != null) {
              val itemLoc = Location(address.latitude, address.longitude)
              GeoUtil.calculateDistanceKm(userLocation, itemLoc)
            } else {
              Double.MAX_VALUE
            }
          }
        } else {
          result
        }
      }
    }

    return result
  }

  private fun getSortDate(item: TicketListItem): String {
    return when (item) {
      is TicketListItem.Remote -> item.ticket.createdAt
      is TicketListItem.Local -> item.draft.lastModified
    }
  }

  private fun getTitle(item: TicketListItem): String {
    return when (item) {
      is TicketListItem.Remote -> item.ticket.title
      is TicketListItem.Local -> item.draft.title
    }
  }
}