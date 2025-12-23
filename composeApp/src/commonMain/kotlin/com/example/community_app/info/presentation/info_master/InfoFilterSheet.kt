package com.example.community_app.info.presentation.info_master

import androidx.compose.runtime.Composable
import com.example.community_app.core.presentation.components.rememberLocationAwareItems
import com.example.community_app.core.presentation.components.search.CommunityFilterSheet
import com.example.community_app.core.presentation.helpers.toUiText
import com.example.community_app.util.InfoCategory
import com.example.community_app.util.InfoStatus
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.distance
import community_app.composeapp.generated.resources.sorting_alphabetical
import community_app.composeapp.generated.resources.sorting_favorites
import community_app.composeapp.generated.resources.sorting_latest
import community_app.composeapp.generated.resources.sorting_oldest
import org.jetbrains.compose.resources.stringResource

@Composable
fun InfoFilterSheet(
  filterState: InfoFilterState,
  onAction: (InfoMasterAction) -> Unit
) {
  val sortOptions = rememberLocationAwareItems(InfoSortOption.entries) { it.requiresLocation }

  CommunityFilterSheet(
    // State
    sortOptions = sortOptions,
    selectedSort = filterState.sortBy,
    categories = InfoCategory.entries,
    selectedCategories = filterState.selectedCategories,
    statuses = InfoStatus.entries,
    selectedStatuses = filterState.selectedStatuses,
    distanceKm = filterState.distanceRadiusKm,
    expandedSections = filterState.expandedSections,

    // Mappers
    sortLabel = { option ->
      when (option) {
        InfoSortOption.DATE_DESC -> stringResource(Res.string.sorting_latest)
        InfoSortOption.DATE_ASC -> stringResource(Res.string.sorting_oldest)
        InfoSortOption.ALPHABETICAL -> stringResource(Res.string.sorting_alphabetical)
        InfoSortOption.FAVORITES -> stringResource(Res.string.sorting_favorites)
        InfoSortOption.DISTANCE -> stringResource(Res.string.distance)
      }
    },
    categoryLabel = { it.toUiText().asString() },
    statusLabel = { it.toUiText().asString() },

    // Actions
    onDismiss = { onAction(InfoMasterAction.OnToggleFilterSheet) },
    onSortChange = { onAction(InfoMasterAction.OnSortChange(it)) },
    onCategorySelect = { onAction(InfoMasterAction.OnCategorySelect(it)) },
    onStatusSelect = { onAction(InfoMasterAction.OnStatusSelect(it)) },
    onDistanceChange = { onAction(InfoMasterAction.OnDistanceChange(it)) },
    onToggleSection = { onAction(InfoMasterAction.OnToggleSection(it)) },
    onClearCategories = { onAction(InfoMasterAction.OnClearCategories) },
    onClearStatuses = { onAction(InfoMasterAction.OnClearStatuses) },
    onClearFilters = {
      onAction(InfoMasterAction.OnSortChange(InfoSortOption.DATE_DESC))
      onAction(InfoMasterAction.OnClearCategories)
      onAction(InfoMasterAction.OnClearStatuses)
      onAction(InfoMasterAction.OnDistanceChange(50f))
    }
  )
}