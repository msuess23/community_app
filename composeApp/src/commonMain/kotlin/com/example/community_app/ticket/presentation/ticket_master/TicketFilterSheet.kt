package com.example.community_app.ticket.presentation.ticket_master

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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.community_app.core.presentation.components.input.CommunityDropdownMenu
import com.example.community_app.core.presentation.components.input.CommunitySlider
import com.example.community_app.core.presentation.components.search.CollapsibleFilterSection
import com.example.community_app.core.presentation.helpers.toUiText
import com.example.community_app.core.presentation.theme.Spacing
import com.example.community_app.util.TicketCategory
import com.example.community_app.util.TicketStatus
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.category_all
import community_app.composeapp.generated.resources.category_plural
import community_app.composeapp.generated.resources.filters_clear
import community_app.composeapp.generated.resources.filters_label
import community_app.composeapp.generated.resources.label_status
import community_app.composeapp.generated.resources.settings_radius_helper
import community_app.composeapp.generated.resources.settings_radius_label
import community_app.composeapp.generated.resources.sorting_alphabetical
import community_app.composeapp.generated.resources.sorting_label
import community_app.composeapp.generated.resources.sorting_latest
import community_app.composeapp.generated.resources.sorting_oldest
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TicketFilterSheet(
  filterState: TicketFilterState,
  onAction: (TicketMasterAction) -> Unit,
  modifier: Modifier = Modifier
) {
  val sheetState = rememberModalBottomSheetState()

  ModalBottomSheet(
    onDismissRequest = { onAction(TicketMasterAction.OnToggleFilterSheet) },
    sheetState = sheetState,
    contentColor = MaterialTheme.colorScheme.surfaceContainer,
    modifier = modifier
  ) {
    Column(
      modifier = Modifier
        .padding(horizontal = 24.dp)
        .padding(bottom = 48.dp)
        .verticalScroll(rememberScrollState())
    ) {
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
        TextButton(
          onClick = {
            onAction(TicketMasterAction.OnSortChange(TicketSortOption.DATE_DESC))
            onAction(TicketMasterAction.OnClearCategories)
            onAction(TicketMasterAction.OnClearStatuses)
            onAction(TicketMasterAction.OnDistanceChange(50f))
            onAction(TicketMasterAction.OnToggleShowDrafts(true))
          }
        ) {
          Text(stringResource(Res.string.filters_clear))
        }
      }

      HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.medium))

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = Spacing.small),
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
          items = TicketSortOption.entries,
          selectedItem = filterState.sortBy,
          onItemSelected = { onAction(TicketMasterAction.OnSortChange(it)) },
          itemLabel = { option ->
            when (option) {
              TicketSortOption.DATE_DESC -> stringResource(Res.string.sorting_latest)
              TicketSortOption.DATE_ASC -> stringResource(Res.string.sorting_oldest)
              TicketSortOption.ALPHABETICAL -> stringResource(Res.string.sorting_alphabetical)
            }
          },
          modifier = Modifier.weight(0.55f)
        )
      }

      HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.medium))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Entwürfe anzeigen", // TODO: Create Res
          style = MaterialTheme.typography.titleMedium
        )
        Switch(
          checked = filterState.showDrafts,
          onCheckedChange = { onAction(TicketMasterAction.OnToggleShowDrafts(it)) }
        )
      }

      HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.medium))

      CollapsibleFilterSection(
        title = Res.string.category_plural,
        isExpanded = filterState.expandedSections.contains(TicketFilterSection.CATEGORY),
        onToggle = { onAction(TicketMasterAction.OnToggleSection(TicketFilterSection.CATEGORY)) }
      ) {
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
          val isAllSelected = filterState.selectedCategories.isEmpty()
          FilterChip(
            selected = isAllSelected,
            onClick = { onAction(TicketMasterAction.OnClearCategories) },
            label = { Text(stringResource(Res.string.category_all)) }
          )
          TicketCategory.entries.forEach { category ->
            FilterChip(
              selected = category in filterState.selectedCategories,
              onClick = { onAction(TicketMasterAction.OnCategorySelect(category)) },
              label = { Text(category.toUiText().asString()) }
            )
          }
        }
      }

      HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.medium))

      CollapsibleFilterSection(
        title = Res.string.label_status,
        isExpanded = filterState.expandedSections.contains(TicketFilterSection.STATUS),
        onToggle = { onAction(TicketMasterAction.OnToggleSection(TicketFilterSection.STATUS)) }
      ) {
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
          val isAllSelected = filterState.selectedStatuses.isEmpty()
          FilterChip(
            selected = isAllSelected,
            onClick = { onAction(TicketMasterAction.OnClearStatuses) },
            label = { Text(stringResource(Res.string.category_all)) }
          )
          TicketStatus.entries.forEach { status ->
            FilterChip(
              selected = status in filterState.selectedStatuses,
              onClick = { onAction(TicketMasterAction.OnStatusSelect(status)) },
              label = { Text(status.toUiText().asString()) }
            )
          }
        }
      }

      HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.medium))

      CollapsibleFilterSection(
        title = Res.string.settings_radius_label,
        isExpanded = filterState.expandedSections.contains(TicketFilterSection.DISTANCE),
        onToggle = { onAction(TicketMasterAction.OnToggleSection(TicketFilterSection.DISTANCE)) }
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.small),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            CommunitySlider(
              value = filterState.distanceRadiusKm,
              onValueChange = { onAction(TicketMasterAction.OnDistanceChange(it)) },
              valueRange = 1f..50f,
              steps = 49,
              modifier = Modifier.weight(1f)
            )
            Text(
              text = "${filterState.distanceRadiusKm.roundToInt()} km",
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