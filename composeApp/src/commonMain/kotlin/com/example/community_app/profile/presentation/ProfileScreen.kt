package com.example.community_app.profile.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.community_app.auth.presentation.components.AuthGuard
import com.example.community_app.core.presentation.components.CommunityTopAppBar
import com.example.community_app.core.presentation.components.ObserveErrorMessage
import com.example.community_app.core.presentation.components.TopBarNavigationType
import com.example.community_app.core.presentation.components.button.CommunityButton
import com.example.community_app.core.presentation.components.button.CommunityOutlinedButton
import com.example.community_app.core.presentation.components.detail.CommunityAddressCard
import com.example.community_app.core.presentation.components.dialog.CommunityDialog
import com.example.community_app.core.presentation.components.input.CommunityCheckbox
import com.example.community_app.core.presentation.components.input.CommunityTextField
import com.example.community_app.core.presentation.theme.Spacing
import com.example.community_app.geocoding.presentation.AddressSearchOverlay
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.auth_forgot_password_dialog_text
import community_app.composeapp.generated.resources.auth_forgot_password_dialog_title
import community_app.composeapp.generated.resources.auth_logout_dialog
import community_app.composeapp.generated.resources.auth_logout_label
import community_app.composeapp.generated.resources.auth_name_label
import community_app.composeapp.generated.resources.auth_otp_label
import community_app.composeapp.generated.resources.auth_reset_password_label
import community_app.composeapp.generated.resources.cancel
import community_app.composeapp.generated.resources.profile_label
import compose.icons.FeatherIcons
import compose.icons.feathericons.Check
import compose.icons.feathericons.Edit2
import compose.icons.feathericons.LogOut
import compose.icons.feathericons.User
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProfileScreenRoot(
  viewModel: ProfileViewModel = koinViewModel(),
  onOpenDrawer: () -> Unit,
  onNavigateToLogin: () -> Unit,
  onNavigateToReset: (String) -> Unit
) {
  val state by viewModel.state.collectAsStateWithLifecycle()

  ProfileScreen(
    state = state,
    onAction = { action ->
      when (action) {
        ProfileAction.OnLoginClick -> onNavigateToLogin()
        ProfileAction.OnChangePasswordConfirm -> {
          onNavigateToReset(state.email)
          viewModel.onAction(action)
        }
        else -> viewModel.onAction(action)
      }
    },
    onOpenDrawer = onOpenDrawer,
    onLoginClick = onNavigateToLogin
  )
}

@Composable
private fun ProfileScreen(
  state: ProfileState,
  onAction: (ProfileAction) -> Unit,
  onOpenDrawer: () -> Unit,
  onLoginClick: () -> Unit
) {
  val snackbarHostState = remember { SnackbarHostState() }

  ObserveErrorMessage(state.errorMessage, snackbarHostState)

  Scaffold(
    topBar = {
      CommunityTopAppBar(
        titleContent = { Text(stringResource(Res.string.profile_label)) },
        navigationType = TopBarNavigationType.Drawer,
        onNavigationClick = onOpenDrawer
      )
    },
    snackbarHost = { SnackbarHost(snackbarHostState) }
  ) { padding ->
    AuthGuard(
      onLoginClick = onLoginClick
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(padding)
          .padding(Spacing.screenPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.large)
      ) {
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
          // Avatar Placeholder
          Surface(
            modifier = Modifier.size(100.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant
          ) {
            Column(
              verticalArrangement = Arrangement.Center,
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Icon(
                imageVector = FeatherIcons.User,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          Text(
            text = state.email,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
          )
        }

        CommunityTextField(
          value = state.editName,
          onValueChange = { onAction(ProfileAction.OnDisplayNameChange(it)) },
          label = Res.string.auth_name_label,
          singleLine = true,
          trailingIcon = {
            IconButton(onClick = { onAction(ProfileAction.OnSaveProfile) }) {
              Icon(if (state.isEditing) FeatherIcons.Check else FeatherIcons.Edit2, null)
            }
          }
        )

        CommunityAddressCard(
          address = state.homeAddress,
          onClick = { onAction(ProfileAction.OnAddressSearchActiveChange(true)) },
          label = "Adresse auswählen"
        )

        HorizontalDivider()

        Column(
          modifier = Modifier.padding().fillMaxSize(),
          verticalArrangement = Arrangement.spacedBy(Spacing.medium),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          CommunityOutlinedButton(
            text = Res.string.auth_reset_password_label,
            onClick = { onAction(ProfileAction.OnChangePasswordClick) }
          )

          CommunityButton(
            text = Res.string.auth_logout_label,
            onClick = { onAction(ProfileAction.OnLogoutClick) },
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
  }

  // --- Logout Dialog ---
  if (state.showLogoutDialog) {
    CommunityDialog(
      title = Res.string.auth_logout_label,
      onDismissRequest = { onAction(ProfileAction.OnLogoutCancel) },
      confirmButtonText = Res.string.auth_logout_label,
      onConfirm = { onAction(ProfileAction.OnLogoutConfirm) },
      dismissButtonText = Res.string.cancel,
      onDismiss = { onAction(ProfileAction.OnLogoutCancel) }
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.medium)
      ) {
        Text(
          text = stringResource(Res.string.auth_logout_dialog),
          style = MaterialTheme.typography.bodyMedium
        )

        CommunityCheckbox(
          label = Res.string.auth_otp_label, // TODO
          checked = state.isLogoutClearDataChecked,
          onCheckChange = { onAction(ProfileAction.OnLogoutClearDataChange(!state.isLogoutClearDataChecked)) }
        )
      }
    }
  }

  // --- Reset Password Dialog ---
  if (state.showPasswordResetDialog) {
    CommunityDialog(
      title = Res.string.auth_forgot_password_dialog_title,
      text = Res.string.auth_forgot_password_dialog_text,
      onDismissRequest = { },
      confirmButtonText = Res.string.auth_otp_label,
      onConfirm = { onAction(ProfileAction.OnChangePasswordConfirm) }
    )
  }

  if (state.isAddressSearchActive) {
    AddressSearchOverlay(
      query = state.addressSearchQuery,
      onQueryChange = { onAction(ProfileAction.OnAddressQueryChange(it)) },
      suggestions = state.addressSugestions,
      onAddressClick = { onAction(ProfileAction.OnSelectAddress(it)) },
      onBackClick = { onAction(ProfileAction.OnAddressSearchActiveChange(false)) }
    )
  }
}