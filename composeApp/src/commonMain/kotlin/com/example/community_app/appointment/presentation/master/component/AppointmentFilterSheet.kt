package com.example.community_app.appointment.presentation.master.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.community_app.appointment.presentation.master.AppointmentFilterState
import com.example.community_app.appointment.presentation.master.AppointmentMasterAction
import com.example.community_app.appointment.presentation.master.AppointmentSortOption
import com.example.community_app.core.presentation.components.input.CommunityDropdownMenu
import com.example.community_app.core.presentation.theme.Spacing
import com.example.community_app.office.presentation.office_detail.component.CommunityDatePicker
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.filters_clear
import community_app.composeapp.generated.resources.filters_label
import community_app.composeapp.generated.resources.from
import community_app.composeapp.generated.resources.sorting_label
import community_app.composeapp.generated.resources.sorting_latest
import community_app.composeapp.generated.resources.sorting_oldest
import community_app.composeapp.generated.resources.until
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentFilterSheet(
  filterState: AppointmentFilterState,
  onAction: (AppointmentMasterAction) -> Unit
) {
  val sheetState = rememberModalBottomSheetState()

  var showStartDatePicker by remember { mutableStateOf(false) }
  var showEndDatePicker by remember { mutableStateOf(false) }

  ModalBottomSheet(
    onDismissRequest = { onAction(AppointmentMasterAction.OnToggleFilterSheet) },
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
        TextButton(onClick = { onAction(AppointmentMasterAction.OnClearFilters) }) {
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
          items = AppointmentSortOption.entries,
          selectedItem = filterState.sortOption,
          onItemSelected = { onAction(AppointmentMasterAction.OnSortChange(it)) },
          itemLabel = { option ->
            when(option) {
              AppointmentSortOption.DATE_ASC -> stringResource(Res.string.sorting_oldest)
              AppointmentSortOption.DATE_DESC -> stringResource(Res.string.sorting_latest)
            }
          },
          modifier = Modifier.weight(0.55f)
        )
      }

      HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.medium))

      DateFilterField(
        label = stringResource(Res.string.from),
        selectedDateMillis = filterState.startDate,
        onFieldClick = { showStartDatePicker = true },
        onClearClick = { onAction(AppointmentMasterAction.OnStartDateSelect(null)) }
      )

      Spacer(modifier = Modifier.height(Spacing.small))

      DateFilterField(
        label = stringResource(Res.string.until),
        selectedDateMillis = filterState.endDate,
        onFieldClick = { showEndDatePicker = true },
        onClearClick = { onAction(AppointmentMasterAction.OnEndDateSelect(null)) }
      )
    }
  }

  if (showStartDatePicker) {
    CommunityDatePicker(
      initialDateMillis = filterState.startDate,
      onDateSelected = {
        onAction(AppointmentMasterAction.OnStartDateSelect(it))
        showStartDatePicker = false
      },
      onDismiss = { showStartDatePicker = false }
    )
  }

  if (showEndDatePicker) {
    CommunityDatePicker(
      initialDateMillis = filterState.endDate,
      onDateSelected = {
        onAction(AppointmentMasterAction.OnEndDateSelect(it))
        showEndDatePicker = false
      },
      onDismiss = { showEndDatePicker = false }
    )
  }
}