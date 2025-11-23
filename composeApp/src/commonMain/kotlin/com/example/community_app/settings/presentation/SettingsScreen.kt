package com.example.community_app.settings.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.community_app.core.presentation.components.CommunityTopAppBar
import com.example.community_app.core.presentation.components.TopBarNavigationType
import com.example.community_app.util.AppLanguage
import com.example.community_app.util.AppTheme
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.cancel
import community_app.composeapp.generated.resources.logout_dialog
import community_app.composeapp.generated.resources.logout_label
import community_app.composeapp.generated.resources.settings_label
import community_app.composeapp.generated.resources.settings_lang_dialog_confirm
import community_app.composeapp.generated.resources.settings_lang_dialog_text
import community_app.composeapp.generated.resources.settings_lang_dialog_title
import community_app.composeapp.generated.resources.settings_lang_english
import community_app.composeapp.generated.resources.settings_lang_german
import community_app.composeapp.generated.resources.settings_lang_label
import community_app.composeapp.generated.resources.settings_lang_system
import community_app.composeapp.generated.resources.settings_theme_dark
import community_app.composeapp.generated.resources.settings_theme_label
import community_app.composeapp.generated.resources.settings_theme_light
import community_app.composeapp.generated.resources.settings_theme_system
import compose.icons.FeatherIcons
import compose.icons.feathericons.Check
import compose.icons.feathericons.Globe
import compose.icons.feathericons.LogOut
import compose.icons.feathericons.Moon
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreenRoot(
  viewModel: SettingsViewModel = koinViewModel(),
  onOpenDrawer: () -> Unit
) {
  val state by viewModel.state.collectAsStateWithLifecycle()

  SettingsScreen(
    state = state,
    onAction = viewModel::onAction,
    onOpenDrawer = onOpenDrawer
  )
}

@Composable
private fun SettingsScreen(
  state: SettingsState,
  onAction: (SettingsAction) -> Unit,
  onOpenDrawer: () -> Unit
) {
  Scaffold(
    topBar = {
      CommunityTopAppBar(
        titleContent = {
          Text(
            text = stringResource(Res.string.settings_label),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
          )
        },
        navigationType = TopBarNavigationType.Drawer,
        onNavigationClick = onOpenDrawer
      )
    },
    containerColor = MaterialTheme.colorScheme.background
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .verticalScroll(rememberScrollState())
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(24.dp)
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

      // --- Language Dialog ---
      if (state.pendingLanguage != null) {
        AlertDialog(
          onDismissRequest = { onAction(SettingsAction.OnLanguageDismiss) },
          icon = {
            Icon(
              imageVector = FeatherIcons.Globe,
              contentDescription = null
            )
          },
          title = {
            Text(stringResource(Res.string.settings_lang_dialog_title))
          },
          text = {
            Text(stringResource(Res.string.settings_lang_dialog_text))
          },
          confirmButton = {
            TextButton(
              onClick = { onAction(SettingsAction.OnLanguageConfirm) }
            ) {
              Text(stringResource(Res.string.settings_lang_dialog_confirm))
            }
          },
          dismissButton = {
            TextButton(
              onClick = { onAction(SettingsAction.OnLanguageDismiss) }
            ) {
              Text(stringResource(Res.string.cancel))
            }
          }
        )
      }

      HorizontalDivider()

      // --- Logout ---
      Button(
        onClick = { onAction(SettingsAction.OnLogoutClick) },
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.error,
          contentColor = MaterialTheme.colorScheme.onError
        ),
        modifier = Modifier.fillMaxWidth()
      ) {
        Icon(
          imageVector = FeatherIcons.LogOut,
          contentDescription = null
        )
        Spacer(modifier = Modifier.padding(4.dp))
        Text(stringResource(Res.string.logout_label))
      }
    }

    // --- Logout Dialog ---
    if (state.showLogoutDialog) {
      AlertDialog(
        onDismissRequest = { onAction(SettingsAction.OnLogoutCancel) },
        title = { Text(stringResource(Res.string.logout_label)) },
        text = { Text(stringResource(Res.string.logout_dialog)) },
        confirmButton = {
          TextButton(
            onClick = { onAction(SettingsAction.OnLogoutConfirm) }
          ) {
            Text(stringResource(Res.string.logout_label))
          }
        },
        dismissButton = {
          TextButton(
            onClick = { onAction(SettingsAction.OnLogoutCancel) }
          ) {
            Text(stringResource(Res.string.cancel))
          }
        }
      )
    }
  }
}

@Composable
private fun SettingsSection(
  title: StringResource,
  icon: ImageVector,
  content: @Composable () -> Unit
) {
  Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(end = 12.dp)
      )
      Text(
        text = stringResource(title),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
        fontWeight = FontWeight.SemiBold
      )
    }
    content()
  }
}

@Composable
private fun <T> SettingsChipGroup(
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