package com.example.community_app.settings.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.height
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import compose.icons.FeatherIcons
import compose.icons.feathericons.Check
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun <T> SettingsChipGroup(
  items: List<T>,
  selectedItem: T,
  onItemSelected: (T) -> Unit,
  labelMapper: (T) -> StringResource
) {
  FlowRow(
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    items.forEach { item ->
      val isSelected = item == selectedItem

      FilterChip(
        selected = isSelected,
        onClick = { onItemSelected(item) },
        label = {
          Text(
            text = stringResource(labelMapper(item))
          )
        },
        leadingIcon = if (isSelected) { {
          Icon(
            imageVector = FeatherIcons.Check,
            contentDescription = null,
            modifier = Modifier.height(18.dp)
          )
        } } else null
      )
    }
  }
}