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
import community_app.composeapp.generated.resources.appointment_plural
import community_app.composeapp.generated.resources.category_plural
import community_app.composeapp.generated.resources.info_plural
import community_app.composeapp.generated.resources.settings_general_label
import community_app.composeapp.generated.resources.settings_notifications_appointment_desc
import community_app.composeapp.generated.resources.settings_notifications_general_desc
import community_app.composeapp.generated.resources.settings_notifications_general_title
import community_app.composeapp.generated.resources.settings_notifications_info_desc
import community_app.composeapp.generated.resources.settings_notifications_ticket_desc
import community_app.composeapp.generated.resources.ticket_plural
import compose.icons.FeatherIcons
import compose.icons.feathericons.Bell
import compose.icons.feathericons.Settings
import org.jetbrains.compose.resources.stringResource

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
      title = Res.string.settings_general_label,
      icon = FeatherIcons.Settings
    ) {
      SettingsSwitchRow(
        label = stringResource(Res.string.settings_notifications_general_title),
        subtitle = stringResource(Res.string.settings_notifications_general_desc),
        checked = state.settings.notificationsEnabled,
        onCheckedChange = { onAction(SettingsAction.OnToggleNotifications(it)) }
      )
    }

    HorizontalDivider()

    SettingsSection(
      title = Res.string.category_plural,
      icon = FeatherIcons.Bell
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Tickets
        SettingsSwitchRow(
          label = stringResource(Res.string.ticket_plural),
          subtitle = stringResource(Res.string.settings_notifications_ticket_desc),
          checked = state.settings.notifyTickets,
          enabled = state.settings.notificationsEnabled,
          onCheckedChange = { onAction(SettingsAction.OnToggleNotifyTickets(it)) }
        )

        // Infos
        SettingsSwitchRow(
          label = stringResource(Res.string.info_plural),
          subtitle = stringResource(Res.string.settings_notifications_info_desc),
          checked = state.settings.notifyInfos,
          enabled = state.settings.notificationsEnabled,
          onCheckedChange = { onAction(SettingsAction.OnToggleNotifyInfos(it)) }
        )

        // Appointments
        SettingsSwitchRow(
          label = stringResource(Res.string.appointment_plural),
          subtitle = stringResource(Res.string.settings_notifications_appointment_desc),
          checked = state.settings.notifyAppointments,
          enabled = state.settings.notificationsEnabled,
          onCheckedChange = { onAction(SettingsAction.OnToggleNotifyAppointments(it)) }
        )

        if (state.settings.notifyAppointments && state.settings.notificationsEnabled) {
          ReminderOffsetSelector(
            currentMinutes = state.settings.appointmentReminderOffsetMinutes,
            onSelect = { onAction(SettingsAction.OnChangeAppointmentReminderOffset(it)) }
          )
        }
      }
    }
  }
}