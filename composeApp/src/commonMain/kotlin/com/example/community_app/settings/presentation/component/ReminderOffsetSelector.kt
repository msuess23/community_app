package com.example.community_app.settings.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.community_app.core.presentation.components.input.CommunityDropdownMenu
import com.example.community_app.core.presentation.theme.Spacing
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.sorting_label
import org.jetbrains.compose.resources.stringResource

@Composable
fun ReminderOffsetSelector(
  currentMinutes: Int,
  onSelect: (Int) -> Unit
) {
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
      items = ReminderOptions,
      selectedItem = selectedSort,
      onItemSelected = ,
      itemLabel = { sortLabel(it) },
      modifier = Modifier.weight(0.55f)
    )
  }
}

private val ReminderOptions = listOf(
  15 to "15 Minuten vorher",
  30 to "30 Minuten vorher",
  60 to "1 Stunde vorher",
  120 to "2 Stunden vorher",
  180 to "3 Stunden vorher",
  1440 to "1 Tag vorher"
)