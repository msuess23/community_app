package com.example.community_app.auth.presentation.reset_password

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.community_app.auth.presentation.components.PasswordTextField
import com.example.community_app.core.presentation.components.CommunityTopAppBar
import com.example.community_app.core.presentation.components.ObserveErrorMessage
import com.example.community_app.core.presentation.components.TopBarNavigationType
import com.example.community_app.core.presentation.components.button.CommunityButton
import com.example.community_app.core.presentation.components.dialog.CommunityDialog
import com.example.community_app.core.presentation.components.input.CommunityTextField
import com.example.community_app.core.presentation.theme.Spacing
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.auth_new_password_label
import community_app.composeapp.generated.resources.auth_new_password_repeat_label
import community_app.composeapp.generated.resources.auth_otp_label
import community_app.composeapp.generated.resources.auth_reset_password_dialog_text
import community_app.composeapp.generated.resources.auth_reset_password_dialog_title
import community_app.composeapp.generated.resources.auth_reset_password_label
import community_app.composeapp.generated.resources.next
import compose.icons.FeatherIcons
import compose.icons.feathericons.Hash
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ResetPasswordScreenRoot(
  viewModel: ResetPasswordViewModel = koinViewModel(),
  onSuccess: () -> Unit,
  onNavigateBack: () -> Unit
) {
  val state by viewModel.state.collectAsStateWithLifecycle()

  ResetPasswordScreen(
    state = state,
    onAction = { action ->
      when(action) {
        ResetPasswordAction.OnNavigateBack -> onNavigateBack()
        ResetPasswordAction.OnSuccessConfirm -> onSuccess()
        else -> viewModel.onAction(action)
      }
    }
  )
}

@Composable
private fun ResetPasswordScreen(
  state: ResetPasswordState,
  onAction: (ResetPasswordAction) -> Unit
) {
  val snackbarHostState = remember { SnackbarHostState() }

  ObserveErrorMessage(
    errorMessage = state.errorMessage,
    snackbarHostState = snackbarHostState,
    isLoading = state.isLoading
  )

  Scaffold(
    topBar = {
      CommunityTopAppBar(
        titleContent = { Text(stringResource(Res.string.auth_reset_password_label)) },
        navigationType = TopBarNavigationType.Back,
        onNavigationClick = { onAction(ResetPasswordAction.OnNavigateBack) }
      )
    },
    snackbarHost = { SnackbarHost(snackbarHostState) }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .verticalScroll(rememberScrollState())
        .padding(Spacing.screenPadding),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      CommunityTextField(
        value = state.otp,
        onValueChange = { onAction(ResetPasswordAction.OnOtpChange(it)) },
        label = Res.string.auth_otp_label,
        leadingIcon = { Icon(FeatherIcons.Hash, null) },
        keyboardOptions = KeyboardOptions(
          keyboardType = KeyboardType.NumberPassword,
          imeAction = ImeAction.Next
        )
      )

      Spacer(modifier = Modifier.height(Spacing.medium))

      PasswordTextField(
        label = Res.string.auth_new_password_label,
        value = state.password,
        onValueChange = { onAction(ResetPasswordAction.OnPasswordChange(it)) },
        isPasswordVisible = state.isPasswordVisible,
        onTogglePasswordVisibility = { onAction(ResetPasswordAction.OnTogglePasswordVisibility) },
        imeAction = ImeAction.Next
      )

      Spacer(modifier = Modifier.height(Spacing.medium))

      PasswordTextField(
        label = Res.string.auth_new_password_repeat_label,
        value = state.passwordRepeat,
        onValueChange = { onAction(ResetPasswordAction.OnPasswordRepeatChange(it)) },
        isPasswordVisible = state.isPasswordVisible,
        onTogglePasswordVisibility = { onAction(ResetPasswordAction.OnTogglePasswordVisibility) }
      )

      Spacer(modifier = Modifier.height(Spacing.large))

      CommunityButton(
        text = Res.string.auth_reset_password_label,
        onClick = { onAction(ResetPasswordAction.OnSubmit) },
        isLoading = state.isLoading
      )
    }

    if (state.showSuccessDialog) {
      CommunityDialog(
        title = Res.string.auth_reset_password_dialog_title,
        text = Res.string.auth_reset_password_dialog_text,
        onDismissRequest = { },
        confirmButtonText = Res.string.next,
        onConfirm = { onAction(ResetPasswordAction.OnSuccessConfirm) }
      )
    }
  }
}