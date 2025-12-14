package com.example.community_app.office.presentation.office_master.component

import androidx.compose.runtime.Composable
import com.example.community_app.core.presentation.components.search.CommunityFilterSheet
import com.example.community_app.office.presentation.office_master.OfficeFilterState
import com.example.community_app.office.presentation.office_master.OfficeMasterAction
import com.example.community_app.office.presentation.office_master.OfficeSortOption
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.distance
import community_app.composeapp.generated.resources.sorting_alphabetical
import org.jetbrains.compose.resources.stringResource

@Composable
fun OfficeFilterSheet(
  filterState: OfficeFilterState,
  onAction: (OfficeMasterAction) -> Unit
) {
  CommunityFilterSheet(
    // State
    sortOptions = OfficeSortOption.entries,
    selectedSort = filterState.sortBy,
    categories = emptyList<String>(),
    selectedCategories = emptySet(),
    statuses = emptyList<String>(),
    selectedStatuses = emptySet(),
    distanceKm = filterState.distanceRadiusKm,
    expandedSections = filterState.expandedSections,
    showCategory = false,
    showStatus = false,

    // Mappers
    sortLabel = { option ->
      when(option) {
        OfficeSortOption.ALPHABETICAL -> stringResource(Res.string.sorting_alphabetical)
        OfficeSortOption.DISTANCE -> stringResource(Res.string.distance)
      }
    },
    categoryLabel = { "" },
    statusLabel = { "" },

    // Actions
    onDismiss = { onAction(OfficeMasterAction.OnToggleFilterSheet) },
    onSortChange = { onAction(OfficeMasterAction.OnSortChange(it)) },
    onCategorySelect = { },
    onStatusSelect = { },
    onDistanceChange = { onAction(OfficeMasterAction.OnDistanceChange(it)) },
    onToggleSection = { onAction(OfficeMasterAction.OnToggleSection(it)) },
    onClearCategories = { },
    onClearStatuses = { },
    onClearFilters = {
      onAction(OfficeMasterAction.OnSortChange(OfficeSortOption.ALPHABETICAL))
      onAction(OfficeMasterAction.OnDistanceChange(50f))
    }
  )
}