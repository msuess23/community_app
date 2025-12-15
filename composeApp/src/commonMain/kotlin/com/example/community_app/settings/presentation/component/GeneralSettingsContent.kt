package com.example.community_app.settings.presentation.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.community_app.core.presentation.theme.Spacing
import com.example.community_app.settings.presentation.SettingsAction
import com.example.community_app.settings.presentation.SettingsState
import com.example.community_app.util.AppLanguage
import com.example.community_app.util.AppTheme
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.appointment_plural
import community_app.composeapp.generated.resources.settings_calendar_desc
import community_app.composeapp.generated.resources.settings_lang_english
import community_app.composeapp.generated.resources.settings_lang_german
import community_app.composeapp.generated.resources.settings_lang_label
import community_app.composeapp.generated.resources.settings_lang_system
import community_app.composeapp.generated.resources.settings_theme_dark
import community_app.composeapp.generated.resources.settings_theme_label
import community_app.composeapp.generated.resources.settings_theme_light
import community_app.composeapp.generated.resources.settings_theme_system
import compose.icons.FeatherIcons
import compose.icons.feathericons.Calendar
import compose.icons.feathericons.Globe
import compose.icons.feathericons.Moon
import org.jetbrains.compose.resources.stringResource

@Composable
fun GeneralSettingsContent(
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
    // --- Theme ---
    SettingsSection(
      title = Res.string.settings_theme_label,
      icon = FeatherIcons.Moon
    ) {
      SettingsChipGroup(
        items = AppTheme.entries,
        selectedItem = state.settings.theme,
        onItemSelected = { onAction(SettingsAction.OnThemeChange(it)) },
        labelMapper = { theme ->
          when (theme) {
            AppTheme.SYSTEM -> Res.string.settings_theme_system
            AppTheme.LIGHT -> Res.string.settings_theme_light
            AppTheme.DARK -> Res.string.settings_theme_dark
          }
        }
      )
    }

    HorizontalDivider()

    // --- Language ---
    SettingsSection(
      title = Res.string.settings_lang_label,
      icon = FeatherIcons.Globe
    ) {
      SettingsChipGroup(
        items = AppLanguage.entries,
        selectedItem = state.settings.language,
        onItemSelected = { onAction(SettingsAction.OnLanguageSelect(it)) },
        labelMapper = { lang ->
          when (lang) {
            AppLanguage.SYSTEM -> Res.string.settings_lang_system
            AppLanguage.GERMAN -> Res.string.settings_lang_german
            AppLanguage.ENGLISH -> Res.string.settings_lang_english
          }
        }
      )
    }

    HorizontalDivider()

    // --- Calendar Sync ---
    SettingsSection(
      title = Res.string.appointment_plural,
      icon = FeatherIcons.Calendar
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = stringResource(Res.string.settings_calendar_desc),
          style = MaterialTheme.typography.bodyLarge
        )
        Switch(
          checked = state.settings.calendarSyncEnabled,
          onCheckedChange = { isChecked ->
            onAction(SettingsAction.OnToggleCalendarSync(isChecked))
          }
        )
      }
    }
  }
}