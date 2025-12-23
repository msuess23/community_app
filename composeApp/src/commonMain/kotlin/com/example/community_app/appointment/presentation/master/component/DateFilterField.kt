package com.example.community_app.appointment.presentation.master.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.community_app.core.util.formatMillisDate
import compose.icons.FeatherIcons
import compose.icons.feathericons.Calendar
import compose.icons.feathericons.X

@Composable
fun DateFilterField(
  label: String,
  selectedDateMillis: Long?,
  onFieldClick: () -> Unit,
  onClearClick: () -> Unit
) {
  val displayText = if (selectedDateMillis != null) {
    formatMillisDate(selectedDateMillis)
  } else ""

  OutlinedTextField(
    value = displayText,
    onValueChange = {},
    label = { Text(label) },
    readOnly = true,
    trailingIcon = {
      if (selectedDateMillis != null) {
        IconButton(onClick = onClearClick) {
          Icon(FeatherIcons.X, contentDescription = "Clear")
        }
      } else {
        Icon(FeatherIcons.Calendar, contentDescription = null)
      }
    },
    enabled = false,
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onFieldClick),
    colors = OutlinedTextFieldDefaults.colors(
      disabledTextColor = MaterialTheme.colorScheme.onSurface,
      disabledBorderColor = MaterialTheme.colorScheme.outline,
      disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
      disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
  )
}