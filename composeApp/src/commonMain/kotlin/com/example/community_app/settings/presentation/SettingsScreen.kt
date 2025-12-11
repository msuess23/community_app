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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.community_app.auth.presentation.components.AuthGuard
import com.example.community_app.auth.presentation.reset_password.ResetPasswordAction
import com.example.community_app.core.presentation.components.CommunityTopAppBar
import com.example.community_app.core.presentation.components.TopBarNavigationType
import com.example.community_app.core.presentation.components.button.CommunityButton
import com.example.community_app.core.presentation.components.button.CommunityOutlinedButton
import com.example.community_app.core.presentation.components.dialog.CommunityDialog
import com.example.community_app.core.presentation.theme.Spacing
import com.example.community_app.util.AppLanguage
import com.example.community_app.util.AppTheme
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.appointment_plural
import community_app.composeapp.generated.resources.auth_forgot_password_dialog_text
import community_app.composeapp.generated.resources.auth_forgot_password_dialog_title
import community_app.composeapp.generated.resources.auth_login_label
import community_app.composeapp.generated.resources.cancel
import community_app.composeapp.generated.resources.auth_logout_dialog
import community_app.composeapp.generated.resources.auth_logout_label
import community_app.composeapp.generated.resources.auth_otp_label
import community_app.composeapp.generated.resources.auth_reset_password_label
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
import compose.icons.feathericons.Calendar
import compose.icons.feathericons.Check
import compose.icons.feathericons.Globe
import compose.icons.feathericons.LogIn
import compose.icons.feathericons.LogOut
import compose.icons.feathericons.Moon
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsScreenRoot(
  viewModel: SettingsViewModel = koinViewModel(),
  onOpenDrawer: () -> Unit,
  onNavigateToLogin: () -> Unit,
  onNavigateToReset: (String) -> Unit
) {
  val state by viewModel.state.collectAsStateWithLifecycle()

  val lifecycleOwner = LocalLifecycleOwner.current
  DisposableEffect(lifecycleOwner) {
    val observer = LifecycleEventObserver { _, event ->
      if (event == Lifecycle.Event.ON_RESUME) {
        viewModel.onAction(SettingsAction.OnResume)
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
    }
  }

  SettingsScreen(
    state = state,
    onAction = { action ->
      when (action) {
        SettingsAction.OnLoginClick -> onNavigateToLogin()
        SettingsAction.OnChangePasswordConfirm -> {
          onNavigateToReset(state.currentUserEmail ?: "")
          viewModel.onAction(action)
        }
        else -> viewModel.onAction(action)
      }
    },
    onOpenDrawer = onOpenDrawer,
  )
}

@Composable
private fun SettingsScreen(
  state: SettingsState,
  onAction: (SettingsAction) -> Unit,
  onOpenDrawer: () -> Unit
) {
  val snackbarHostState = remember { SnackbarHostState() }

  LaunchedEffect(state.showCalendarPermissionRationale) {
    if (state.showCalendarPermissionRationale) {
      val result = snackbarHostState.showSnackbar(
        message = "Kalenderzugriff erforderlich", // TODO: Localize
        actionLabel = "Einstellungen", // TODO: Localize
        duration = SnackbarDuration.Long
      )
      if (result == SnackbarResult.ActionPerformed) {
        onAction(SettingsAction.OnOpenSettings)
      }
    }
  }


  Scaffold(
    topBar = {
      CommunityTopAppBar(
        titleContent = { Text(stringResource(Res.string.settings_label)) },
        navigationType = TopBarNavigationType.Drawer,
        onNavigationClick = onOpenDrawer
      )
    },
    snackbarHost = { SnackbarHost((snackbarHostState)) },
    containerColor = MaterialTheme.colorScheme.background
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
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

      // --- Language Dialog ---
      if (state.pendingLanguage != null) {
        CommunityDialog(
          title = Res.string.settings_lang_dialog_title,
          text = Res.string.settings_lang_dialog_text,
          onDismissRequest = { onAction(SettingsAction.OnLanguageDismiss) },
          confirmButtonText = Res.string.settings_lang_dialog_confirm,
          onConfirm = { onAction(SettingsAction.OnLanguageConfirm) },
          dismissButtonText = Res.string.cancel,
          onDismiss = { onAction(SettingsAction.OnLanguageDismiss) },
          icon = FeatherIcons.Globe
        )
      }

      HorizontalDivider()

      // --- Calendar Sync ---
      if (state.currentUserEmail != null) {
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
              text = "Mit Kalender synchronisieren", // TODO: Localize
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
        HorizontalDivider()
      }

      // --- Logout ---
      AuthGuard(
        onLoginClick = { },
        fallbackContent = {
          CommunityButton(
            text = Res.string.auth_login_label,
            onClick = { onAction(SettingsAction.OnLoginClick) },
            icon = FeatherIcons.LogIn
          )
        }
      ) {
        Column(
          modifier = Modifier.padding().fillMaxSize(),
          verticalArrangement = Arrangement.spacedBy(Spacing.medium),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          CommunityOutlinedButton(
            text = Res.string.auth_reset_password_label,
            onClick = { onAction(SettingsAction.OnChangePasswordClick) }
          )

          CommunityButton(
            text = Res.string.auth_logout_label,
            onClick = { onAction(SettingsAction.OnLogoutClick) },
            isLoading = state.isLoading,
            icon = FeatherIcons.LogOut,
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.error,
              contentColor = MaterialTheme.colorScheme.onError
            ),
          )
        }
      }
    }

    // --- Logout Dialog ---
    if (state.showLogoutDialog) {
      CommunityDialog(
        title = Res.string.auth_logout_label,
        text = Res.string.auth_logout_dialog,
        onDismissRequest = { onAction(SettingsAction.OnLogoutCancel) },
        confirmButtonText = Res.string.auth_logout_label,
        onConfirm = { onAction(SettingsAction.OnLogoutConfirm) },
        dismissButtonText = Res.string.cancel,
        onDismiss = { onAction(SettingsAction.OnLogoutCancel) }
      )
    }

    if (state.showPasswordResetDialog) {
      CommunityDialog(
        title = Res.string.auth_forgot_password_dialog_title,
        text = Res.string.auth_forgot_password_dialog_text,
        onDismissRequest = { },
        confirmButtonText = Res.string.auth_otp_label,
        onConfirm = { onAction(SettingsAction.OnChangePasswordConfirm) }
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