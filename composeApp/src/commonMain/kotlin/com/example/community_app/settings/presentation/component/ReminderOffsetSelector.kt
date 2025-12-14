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
import community_app.composeapp.generated.resources.settings_notifications_reminder_120m
import community_app.composeapp.generated.resources.settings_notifications_reminder_180m
import community_app.composeapp.generated.resources.settings_notifications_reminder_1d
import community_app.composeapp.generated.resources.settings_notifications_reminder_30m
import community_app.composeapp.generated.resources.settings_notifications_reminder_60m
import community_app.composeapp.generated.resources.settings_notifications_reminder_label
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ReminderOffsetSelector(
  currentMinutes: Int,
  onSelect: (Int) -> Unit
) {
  val selectedOption = ReminderOptions.find { it.first == currentMinutes }
    ?: ReminderOptions.firstOrNull { it.first == 180 }
    ?: ReminderOptions.first()

  Row(
    modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.small),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Text(
      text = stringResource(Res.string.settings_notifications_reminder_label),
      style = MaterialTheme.typography.bodyLarge,
      color = MaterialTheme.colorScheme.onSurface,
      modifier = Modifier.weight(0.35f)
    )
    CommunityDropdownMenu(
      items = ReminderOptions,
      selectedItem = selectedOption,
      onItemSelected = { (minutes, _) -> onSelect(minutes)},
      itemLabel = { (_, label) -> stringResource(label) },
      modifier = Modifier.weight(0.55f)
    )
  }
}

private val ReminderOptions: List<Pair<Int, StringResource>> = listOf(
  30 to Res.string.settings_notifications_reminder_30m,
  60 to Res.string.settings_notifications_reminder_60m,
  120 to Res.string.settings_notifications_reminder_120m,
  180 to Res.string.settings_notifications_reminder_180m,
  1440 to Res.string.settings_notifications_reminder_1d
)