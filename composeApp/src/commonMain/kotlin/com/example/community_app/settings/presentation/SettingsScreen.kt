package com.example.community_app.settings.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.community_app.core.presentation.components.CommunityTopAppBar
import com.example.community_app.core.presentation.components.TopBarNavigationType
import com.example.community_app.core.presentation.components.dialog.CommunityDialog
import com.example.community_app.settings.presentation.component.GeneralSettingsContent
import com.example.community_app.settings.presentation.component.NotificationSettingsContent
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.auth_forgot_password_dialog_text
import community_app.composeapp.generated.resources.auth_forgot_password_dialog_title
import community_app.composeapp.generated.resources.cancel
import community_app.composeapp.generated.resources.auth_logout_dialog
import community_app.composeapp.generated.resources.auth_logout_label
import community_app.composeapp.generated.resources.auth_otp_label
import community_app.composeapp.generated.resources.settings_label
import community_app.composeapp.generated.resources.settings_lang_dialog_confirm
import community_app.composeapp.generated.resources.settings_lang_dialog_text
import community_app.composeapp.generated.resources.settings_lang_dialog_title
import community_app.composeapp.generated.resources.ticket_ownership_community
import community_app.composeapp.generated.resources.ticket_ownership_user
import compose.icons.FeatherIcons
import compose.icons.feathericons.Globe
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

  val tabs = listOf(Res.string.ticket_ownership_community, Res.string.ticket_ownership_user)

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
//        .padding(Spacing.screenPadding)
    ) {
      SecondaryTabRow(
        selectedTabIndex = state.selectedTabIndex,
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.primary,
        divider = { HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant) }
      ) {
        tabs.forEachIndexed { index, title ->
          Tab(
            selected = state.selectedTabIndex == index,
            onClick = { onAction(SettingsAction.OnTabChange(index)) },
            text = {
              Text(
                text = stringResource(title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (state.selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.primary
              )
            }
          )
        }
      }

      AnimatedContent(
        targetState = state.selectedTabIndex,
        label = "SettingsTabAnimation"
      ) { targetTab ->
        when (targetTab) {
          0 -> GeneralSettingsContent(state, onAction)
          1 -> NotificationSettingsContent(state, onAction)
        }
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

      // --- Reset Password Dialog ---
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
}