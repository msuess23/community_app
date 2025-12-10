package com.example.community_app.office.presentation.office_detail.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.cancel
import org.jetbrains.compose.resources.stringResource

@Composable
fun CommunityDatePicker(
  initialDateMillis: Long,
  dateRange: LongRange,
  onDateSelected: (Long?) -> Unit,
  onDismiss: () -> Unit
) {
  val dateValidator = remember(dateRange) {
    object : SelectableDates {
      override fun isSelectableDate(utcTimeMillis: Long): Boolean {
        return dateRange.contains(utcTimeMillis)
      }
      override fun isSelectableYear(year: Int): Boolean = true
    }
  }

  val datePickerState = rememberDatePickerState(
    initialSelectedDateMillis = initialDateMillis,
    selectableDates = dateValidator
  )

  val datePickerColors = DatePickerDefaults.colors(
    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    titleContentColor = MaterialTheme.colorScheme.onSurface,
    headlineContentColor = MaterialTheme.colorScheme.onSurface,
    weekdayContentColor = MaterialTheme.colorScheme.onSurface,
    subheadContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    dayContentColor = MaterialTheme.colorScheme.onSurface,
    selectedDayContainerColor = MaterialTheme.colorScheme.primary,
    selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
    todayContentColor = MaterialTheme.colorScheme.primary,
    todayDateBorderColor = MaterialTheme.colorScheme.primary
  )

  DatePickerDialog(
    onDismissRequest = onDismiss,
    confirmButton = {
      TextButton(onClick = {
        onDateSelected(datePickerState.selectedDateMillis)
      }) {
        Text("OK") // TODO: Localize
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text(stringResource(Res.string.cancel))
      }
    },
    colors = DatePickerDefaults.colors(
      containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    )
  ) {
    DatePicker(
      state = datePickerState,
      colors = datePickerColors,
      title = {
        Text(
          text = "Datum wählen", // TODO: Localize
          modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp),
          style = MaterialTheme.typography.labelLarge
        )
      }
    )
  }
}