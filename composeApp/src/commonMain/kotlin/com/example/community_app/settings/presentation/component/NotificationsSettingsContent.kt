package com.example.community_app.settings.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.community_app.core.presentation.theme.Spacing
import com.example.community_app.settings.presentation.SettingsAction
import com.example.community_app.settings.presentation.SettingsState
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.settings_label
import compose.icons.FeatherIcons
import compose.icons.feathericons.Bell
import compose.icons.feathericons.Settings

@Composable
fun NotificationSettingsContent(
  state: SettingsState,
  onAction: (SettingsAction) -> Unit
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(Spacing.screenPadding),
    verticalArrangement = Arrangement.spacedBy(Spacing.large)
  ) {
    // --- Master Switch ---
    SettingsSection(
      title = Res.string.settings_label, // Oder "Allgemein"
      icon = FeatherIcons.Settings // Oder Bell
    ) {
      SettingsSwitchRow(
        label = "Benachrichtigungen erlauben", // TODO: Localize
        subtitle = "Push-Benachrichtigungen generell aktivieren", // TODO: Localize
        checked = state.settings.notificationsEnabled,
        onCheckedChange = { onAction(SettingsAction.OnToggleNotifications(it)) }
      )
    }

    HorizontalDivider()

    // --- Kategorien (Nur aktiv wenn Master Switch an ist) ---
    SettingsSection(
      title = Res.string.settings_label, // "Kategorien" (TODO: Localize)
      icon = FeatherIcons.Bell
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Tickets
        SettingsSwitchRow(
          label = "Tickets", // TODO: Localize
          subtitle = "Statusänderungen bei Tickets", // TODO: Localize
          checked = state.settings.notifyTickets,
          enabled = state.settings.notificationsEnabled,
          onCheckedChange = { onAction(SettingsAction.OnToggleNotifyTickets(it)) }
        )

        // Infos
        SettingsSwitchRow(
          label = "Informationen", // TODO: Localize
          subtitle = "Updates zu favorisierten Infos", // TODO: Localize
          checked = state.settings.notifyInfos,
          enabled = state.settings.notificationsEnabled,
          onCheckedChange = { onAction(SettingsAction.OnToggleNotifyInfos(it)) }
        )

        // Appointments
        SettingsSwitchRow(
          label = "Termine", // TODO: Localize
          subtitle = "Erinnerungen vor Terminbeginn", // TODO: Localize
          checked = state.settings.notifyAppointments,
          enabled = state.settings.notificationsEnabled,
          onCheckedChange = { onAction(SettingsAction.OnToggleNotifyAppointments(it)) }
        )
      }
    }
  }
}