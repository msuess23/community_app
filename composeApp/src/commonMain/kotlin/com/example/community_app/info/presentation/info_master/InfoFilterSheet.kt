package com.example.community_app.info.presentation.info_master

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.community_app.core.presentation.components.input.CommunityDropdownMenu
import com.example.community_app.core.presentation.components.search.CollapsibleFilterSection
import com.example.community_app.core.presentation.helpers.toUiText
import com.example.community_app.core.presentation.theme.Spacing
import com.example.community_app.util.InfoCategory
import com.example.community_app.util.InfoStatus
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.category_all
import community_app.composeapp.generated.resources.category_plural
import community_app.composeapp.generated.resources.filters_clear
import community_app.composeapp.generated.resources.filters_label
import community_app.composeapp.generated.resources.label_status
import community_app.composeapp.generated.resources.sorting_alphabetical
import community_app.composeapp.generated.resources.sorting_label
import community_app.composeapp.generated.resources.sorting_latest
import community_app.composeapp.generated.resources.sorting_oldest
import community_app.composeapp.generated.resources.welcome_back
import compose.icons.FeatherIcons
import compose.icons.feathericons.ChevronDown
import compose.icons.feathericons.ChevronUp
import org.jetbrains.compose.resources.stringResource
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoFilterSheet(
  filterState: InfoFilterState,
  onAction: (InfoMasterAction) -> Unit,
  modifier: Modifier = Modifier
) {
  val sheetState = rememberModalBottomSheetState()

  ModalBottomSheet(
    onDismissRequest = { onAction(InfoMasterAction.OnToggleFilterSheet) },
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
            onAction(InfoMasterAction.OnSortChange(InfoSortOption.DATE_DESC))
            onAction(InfoMasterAction.OnClearCategories)
            onAction(InfoMasterAction.OnClearStatuses)
            onAction(InfoMasterAction.OnDistanceChange(50f))
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
          items = InfoSortOption.entries,
          selectedItem = filterState.sortBy,
          onItemSelected = { onAction(InfoMasterAction.OnSortChange(it)) },
          itemLabel = { option ->
            when (option) {
              InfoSortOption.DATE_DESC -> stringResource(Res.string.sorting_latest)
              InfoSortOption.DATE_ASC -> stringResource(Res.string.sorting_oldest)
              InfoSortOption.ALPHABETICAL -> stringResource(Res.string.sorting_alphabetical)
            }
          },
          modifier = Modifier.weight(0.55f)
        )
      }

      HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.medium))

      CollapsibleFilterSection(
        title = Res.string.category_plural,
        isExpanded = filterState.expandedSections.contains(FilterSection.CATEGORY),
        onToggle = { onAction(InfoMasterAction.OnToggleSection(FilterSection.CATEGORY)) }
      ) {
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
          val isAllSelected = filterState.selectedCategories.isEmpty()
          FilterChip(
            selected = isAllSelected,
            onClick = { onAction(InfoMasterAction.OnClearCategories) },
            label = { Text(stringResource(Res.string.category_all)) }
          )
          InfoCategory.entries.forEach { category ->
            FilterChip(
              selected = category in filterState.selectedCategories,
              onClick = { onAction(InfoMasterAction.OnCategorySelect(category)) },
              label = { Text(category.toUiText().asString()) }
            )
          }
        }
      }

      HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.medium))

      CollapsibleFilterSection(
        title = Res.string.label_status,
        isExpanded = filterState.expandedSections.contains(FilterSection.STATUS),
        onToggle = { onAction(InfoMasterAction.OnToggleSection(FilterSection.STATUS)) }
      ) {
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
          val isAllSelected = filterState.selectedStatuses.isEmpty()
          FilterChip(
            selected = isAllSelected,
            onClick = { onAction(InfoMasterAction.OnClearStatuses) },
            label = { Text(stringResource(Res.string.category_all)) }
          )
          InfoStatus.entries.forEach { status ->
            FilterChip(
              selected = status in filterState.selectedStatuses,
              onClick = { onAction(InfoMasterAction.OnStatusSelect(status)) },
              label = { Text(status.toUiText().asString()) }
            )
          }
        }
      }

      HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.medium))

      CollapsibleFilterSection(
        title = Res.string.welcome_back,
        isExpanded = filterState.expandedSections.contains(FilterSection.DISTANCE),
        onToggle = { onAction(InfoMasterAction.OnToggleSection(FilterSection.DISTANCE)) }
      ) {
        Column {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Max. Entfernung") // TODO: Res
            Text("${filterState.distanceRadiusKm.roundToInt()} km")
          }
          Slider(
            value = filterState.distanceRadiusKm,
            onValueChange = { onAction(InfoMasterAction.OnDistanceChange(it)) },
            valueRange = 1f..50f,
            steps = 49
          )
          Text(
            text = "Filtert basierend auf Ihrem aktuellen Standort (benötigt GPS).", // TODO: Res
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }
  }
}

@Composable
private fun SortOptionRow(
  text: String,
  selected: Boolean,
  onClick: () -> Unit
) {
  Row(
    Modifier
      .fillMaxWidth()
      .height(48.dp)
      .selectable(
        selected = selected,
        onClick = onClick,
        role = Role.RadioButton
      ),
    verticalAlignment = Alignment.CenterVertically
  ) {
    RadioButton(
      selected = selected,
      onClick = null
    )
    Spacer(Modifier.padding(start = 16.dp))
    Text(
      text = text,
      style = MaterialTheme.typography.bodyLarge,
      color = MaterialTheme.colorScheme.onSurface,
    )
  }
}