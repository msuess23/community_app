package com.example.community_app.auth.presentation.register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.community_app.auth.presentation.components.AuthHeadline
import com.example.community_app.auth.presentation.components.EmailTextField
import com.example.community_app.auth.presentation.components.PasswordTextField
import com.example.community_app.core.presentation.components.ObserveErrorMessage
import com.example.community_app.core.presentation.components.button.CommunityButton
import com.example.community_app.core.presentation.components.button.CommunityOutlinedButton
import com.example.community_app.core.presentation.components.button.CommunityTextButton
import com.example.community_app.core.presentation.components.input.CommunityTextField
import com.example.community_app.core.presentation.theme.Spacing
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.auth_register_already_has_account
import community_app.composeapp.generated.resources.auth_login_label
import community_app.composeapp.generated.resources.auth_name_label
import community_app.composeapp.generated.resources.auth_progress_without_account
import community_app.composeapp.generated.resources.auth_register_label
import community_app.composeapp.generated.resources.auth_register_title
import community_app.composeapp.generated.resources.auth_password_repeat_label
import compose.icons.FeatherIcons
import compose.icons.feathericons.User
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RegisterScreenRoot(
  viewModel: RegisterViewModel = koinViewModel(),
  onRegisterSuccess: () -> Unit,
  onNavigateToLogin: () -> Unit,
  onNavigateToGuest: () -> Unit
) {
  val state by viewModel.state.collectAsStateWithLifecycle()

  LaunchedEffect(state.isRegisterSuccessful) {
    if (state.isRegisterSuccessful) onRegisterSuccess()
  }

  RegisterScreen(
    state = state,
    onAction = { action ->
      when(action) {
        is RegisterAction.OnLoginClick -> onNavigateToLogin()
        is RegisterAction.OnGuestClick -> onNavigateToGuest()
        else -> viewModel.onAction(action)
      }
    }
  )
}

@Composable
private fun RegisterScreen(
  state: RegisterState,
  onAction: (RegisterAction) -> Unit
) {
  val snackbarHostState = remember { SnackbarHostState() }

  ObserveErrorMessage(
    errorMessage = state.errorMessage,
    snackbarHostState = snackbarHostState,
    isLoading = state.isLoading
  )

  Scaffold(
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
      AuthHeadline(Res.string.auth_register_title)
      Spacer(modifier = Modifier.height(Spacing.extraLarge))

      CommunityTextField(
        value = state.displayName,
        onValueChange = { onAction(RegisterAction.OnDisplayNameChange(it)) },
        label = Res.string.auth_name_label,
        leadingIcon = { Icon(FeatherIcons.User, null) },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
      )

      Spacer(modifier = Modifier.height(Spacing.medium))

      EmailTextField(
        value = state.email,
        onValueChange = { onAction(RegisterAction.OnEmailChange(it)) }
      )

      Spacer(modifier = Modifier.height(Spacing.medium))

      PasswordTextField(
        value = state.password,
        onValueChange = { onAction(RegisterAction.OnPasswordChange(it)) },
        isPasswordVisible = state.isPasswordVisible,
        onTogglePasswordVisibility = { onAction(RegisterAction.OnTogglePasswordVisibility) },
        imeAction = ImeAction.Next
      )

      Spacer(modifier = Modifier.height(Spacing.medium))

      PasswordTextField(
        label = Res.string.auth_password_repeat_label,
        value = state.passwordRepeat,
        onValueChange = { onAction(RegisterAction.OnPasswordRepeatChange(it)) },
        isPasswordVisible = state.isPasswordVisible,
        onTogglePasswordVisibility = { onAction(RegisterAction.OnTogglePasswordVisibility) }
      )

      Spacer(modifier = Modifier.height(Spacing.large))

      CommunityButton(
        text = Res.string.auth_register_label,
        onClick = { onAction(RegisterAction.OnRegisterClick) },
        isLoading = state.isLoading
      )

      Spacer(modifier = Modifier.height(Spacing.medium))

      CommunityOutlinedButton(
        text = Res.string.auth_progress_without_account,
        onClick = { onAction(RegisterAction.OnGuestClick) }
      )

      Spacer(modifier = Modifier.height(Spacing.large))

      Row(
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(stringResource(Res.string.auth_register_already_has_account))
        CommunityTextButton(
          text = Res.string.auth_login_label,
          onClick = { onAction(RegisterAction.OnLoginClick) }
        )
      }
    }
  }
}