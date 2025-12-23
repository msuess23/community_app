package com.example.community_app.core.presentation.components.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.community_app.core.presentation.components.LocationGuard
import com.example.community_app.core.presentation.components.input.CommunityDropdownMenu
import com.example.community_app.core.presentation.components.input.CommunitySlider
import com.example.community_app.core.presentation.theme.Spacing
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.category_all
import community_app.composeapp.generated.resources.category_plural
import community_app.composeapp.generated.resources.filters_clear
import community_app.composeapp.generated.resources.filters_label
import community_app.composeapp.generated.resources.label_status
import community_app.composeapp.generated.resources.settings_radius_helper
import community_app.composeapp.generated.resources.settings_radius_label
import community_app.composeapp.generated.resources.sorting_label
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T_Sort, T_Cat, T_Status> CommunityFilterSheet(
  // State
  sortOptions: List<T_Sort>,
  selectedSort: T_Sort,
  categories: List<T_Cat>,
  selectedCategories: Set<T_Cat>,
  statuses: List<T_Status>,
  selectedStatuses: Set<T_Status>,
  distanceKm: Float,
  expandedSections: Set<FilterSection>,

  // Configuration
  showCategory: Boolean = true,
  showStatus: Boolean = true,
  showDistance: Boolean = true,

  // Label Mappers (Mapping Logic in UI Layer)
  sortLabel: @Composable (T_Sort) -> String,
  categoryLabel: @Composable (T_Cat) -> String,
  statusLabel: @Composable (T_Status) -> String,

  // Actions
  onDismiss: () -> Unit,
  onSortChange: (T_Sort) -> Unit,
  onCategorySelect: (T_Cat) -> Unit,
  onStatusSelect: (T_Status) -> Unit,
  onDistanceChange: (Float) -> Unit,
  onToggleSection: (FilterSection) -> Unit,
  onClearFilters: () -> Unit,
  onClearCategories: () -> Unit,
  onClearStatuses: () -> Unit,

  // Optional Slot for extra content (e.g. "Show Drafts" switch)
  extraContent: (@Composable () -> Unit)? = null
) {
  val sheetState = rememberModalBottomSheetState()

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    contentColor = MaterialTheme.colorScheme.surfaceContainer
  ) {
    Column(
      modifier = Modifier
        .padding(horizontal = 24.dp)
        .padding(bottom = 48.dp)
        .verticalScroll(rememberScrollState())
    ) {
      // Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = stringResource(Res.string.filters_label),
          style = MaterialTheme.typography.titleLarge,
          color = MaterialTheme.colorScheme.onSurface,
          fontWeight = FontWeight.Bold
        )
        TextButton(onClick = onClearFilters) {
          Text(stringResource(Res.string.filters_clear))
        }
      }

      HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.medium))

      // Sorting
      Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.small),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = stringResource(Res.string.sorting_label),
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onSurface,
          modifier = Modifier.weight(0.35f)
        )
        CommunityDropdownMenu(
          items = sortOptions,
          selectedItem = selectedSort,
          onItemSelected = onSortChange,
          itemLabel = { sortLabel(it) },
          modifier = Modifier.weight(0.55f)
        )
      }

      // Extra Content Slot (e.g. Draft Switch)
      if (extraContent != null) {
        HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.medium))
        extraContent()
      }

      // Category
      if (showCategory) {
        CollapsibleFilterSection(
          title = Res.string.category_plural,
          isExpanded = expandedSections.contains(FilterSection.CATEGORY),
          onToggle = { onToggleSection(FilterSection.CATEGORY) }
        ) {
          FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
            FilterChip(
              selected = selectedCategories.isEmpty(),
              onClick = onClearCategories,
              label = { Text(stringResource(Res.string.category_all)) }
            )
            categories.forEach { cat ->
              FilterChip(
                selected = cat in selectedCategories,
                onClick = { onCategorySelect(cat) },
                label = { Text(categoryLabel(cat)) }
              )
            }
          }
        }
      }

      // Status
      if (showStatus) {
        CollapsibleFilterSection(
          title = Res.string.label_status,
          isExpanded = expandedSections.contains(FilterSection.STATUS),
          onToggle = { onToggleSection(FilterSection.STATUS) }
        ) {
          FlowRow(horizontalArrangement = Arrangement.spacedBy(Spacing.small)) {
            FilterChip(
              selected = selectedStatuses.isEmpty(),
              onClick = onClearStatuses,
              label = { Text(stringResource(Res.string.category_all)) }
            )
            statuses.forEach { stat ->
              FilterChip(
                selected = stat in selectedStatuses,
                onClick = { onStatusSelect(stat) },
                label = { Text(statusLabel(stat)) }
              )
            }
          }
        }
      }

      // Distance (optional)
      if (showDistance) {
        LocationGuard {
          CollapsibleFilterSection(
            title = Res.string.settings_radius_label,
            isExpanded = expandedSections.contains(FilterSection.DISTANCE),
            onToggle = { onToggleSection(FilterSection.DISTANCE) }
          ) {
            Column(
              modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.small),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                CommunitySlider(
                  value = distanceKm,
                  onValueChange = onDistanceChange,
                  valueRange = 1f..50f,
                  steps = 49,
                  modifier = Modifier.weight(1f)
                )
                Text(
                  text = "${distanceKm.roundToInt()} km",
                  style = MaterialTheme.typography.titleMedium,
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
              Text(
                text = stringResource(Res.string.settings_radius_helper),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }
    }
  }
}