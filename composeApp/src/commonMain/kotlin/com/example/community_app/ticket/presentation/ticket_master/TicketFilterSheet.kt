package com.example.community_app.ticket.presentation.ticket_master

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.community_app.core.presentation.components.search.CommunityFilterSheet
import com.example.community_app.core.presentation.helpers.toUiText
import com.example.community_app.util.TicketCategory
import com.example.community_app.util.TicketStatus
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.draft_show_filter
import community_app.composeapp.generated.resources.sorting_alphabetical
import community_app.composeapp.generated.resources.sorting_latest
import community_app.composeapp.generated.resources.sorting_oldest
import org.jetbrains.compose.resources.stringResource

@Composable
fun TicketFilterSheet(
  filterState: TicketFilterState,
  isCommunityTab: Boolean,
  onAction: (TicketMasterAction) -> Unit
) {
  CommunityFilterSheet(
    // State
    sortOptions = TicketSortOption.entries,
    selectedSort = filterState.sortBy,
    categories = TicketCategory.entries,
    selectedCategories = filterState.selectedCategories,
    statuses = TicketStatus.entries,
    selectedStatuses = filterState.selectedStatuses,
    distanceKm = filterState.distanceRadiusKm,
    expandedSections = filterState.expandedSections,

    // Config
    showDistance = isCommunityTab,

    // Mappers
    sortLabel = { option ->
      when (option) {
        TicketSortOption.DATE_DESC -> stringResource(Res.string.sorting_latest)
        TicketSortOption.DATE_ASC -> stringResource(Res.string.sorting_oldest)
        TicketSortOption.ALPHABETICAL -> stringResource(Res.string.sorting_alphabetical)
      }
    },
    categoryLabel = { it.toUiText().asString() },
    statusLabel = { it.toUiText().asString() },

    // Actions
    onDismiss = { onAction(TicketMasterAction.OnToggleFilterSheet) },
    onSortChange = { onAction(TicketMasterAction.OnSortChange(it)) },
    onCategorySelect = { onAction(TicketMasterAction.OnCategorySelect(it)) },
    onStatusSelect = { onAction(TicketMasterAction.OnStatusSelect(it)) },
    onDistanceChange = { onAction(TicketMasterAction.OnDistanceChange(it)) },
    onToggleSection = { onAction(TicketMasterAction.OnToggleSection(it)) },
    onClearCategories = { onAction(TicketMasterAction.OnClearCategories) },
    onClearStatuses = { onAction(TicketMasterAction.OnClearStatuses) },
    onClearFilters = {
      onAction(TicketMasterAction.OnSortChange(TicketSortOption.DATE_DESC))
      onAction(TicketMasterAction.OnClearCategories)
      onAction(TicketMasterAction.OnClearStatuses)
      onAction(TicketMasterAction.OnDistanceChange(50f))
      onAction(TicketMasterAction.OnToggleShowDrafts(true))
    },

    // Drafts Switch
    extraContent = if (!isCommunityTab) {
      {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = stringResource(Res.string.draft_show_filter),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Switch(
            checked = filterState.showDrafts,
            onCheckedChange = { onAction(TicketMasterAction.OnToggleShowDrafts(it)) }
          )
        }
      }
    } else null
  )
}