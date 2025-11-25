package com.example.community_app.auth.presentation.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.community_app.auth.presentation.components.AuthHeadline
import com.example.community_app.core.presentation.components.input.CommunityCheckbox
import com.example.community_app.auth.presentation.components.EmailTextField
import com.example.community_app.auth.presentation.components.PasswordTextField
import com.example.community_app.core.presentation.components.ObserveErrorMessage
import com.example.community_app.core.presentation.components.button.CommunityButton
import com.example.community_app.core.presentation.components.button.CommunityOutlinedButton
import com.example.community_app.core.presentation.components.button.CommunityTextButton
import com.example.community_app.core.presentation.theme.Spacing
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.auth_forgot_password_label
import community_app.composeapp.generated.resources.auth_login_label
import community_app.composeapp.generated.resources.auth_login_no_account_yet
import community_app.composeapp.generated.resources.auth_progress_without_account
import community_app.composeapp.generated.resources.auth_register_label
import community_app.composeapp.generated.resources.auth_login_remember
import community_app.composeapp.generated.resources.welcome_back
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginScreenRoot(
  viewModel: LoginViewModel = koinViewModel(),
  onLoginSuccess: () -> Unit,
  onNavigateToRegister: () -> Unit,
  onNavigateToGuest: () -> Unit,
  onNavigateToForgotPassword: () -> Unit
) {
  val state by viewModel.state.collectAsStateWithLifecycle()

  LaunchedEffect(state.isLoginSuccessful) {
    if (state.isLoginSuccessful) onLoginSuccess()
  }

  LoginScreen(
    state = state,
    onAction = { action ->
      when(action) {
        is LoginAction.OnRegisterClick -> onNavigateToRegister()
        is LoginAction.OnGuestClick -> onNavigateToGuest()
        is LoginAction.OnForgotPasswordClick -> onNavigateToForgotPassword()
        else -> viewModel.onAction(action)
      }
    }
  )
}

@Composable
private fun LoginScreen(
  state: LoginState,
  onAction: (LoginAction) -> Unit
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
        .padding(padding)
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(Spacing.screenPadding),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      AuthHeadline(Res.string.welcome_back)
      Spacer(modifier = Modifier.height(Spacing.extraLarge))

      EmailTextField(
        value = state.email,
        onValueChange = { onAction(LoginAction.OnEmailChange(it)) }
      )

      Spacer(modifier = Modifier.height(Spacing.medium))

      PasswordTextField(
        value = state.password,
        onValueChange = { onAction(LoginAction.OnPasswordChange(it)) },
        isPasswordVisible = state.isPasswordVisible,
        onTogglePasswordVisibility = { onAction(LoginAction.OnTogglePasswordVisibility) },
      )

      Spacer(modifier = Modifier.height(Spacing.small))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        CommunityCheckbox(
          label = Res.string.auth_login_remember,
          checked = state.isRememberMeChecked,
          onCheckChange = { onAction(LoginAction.OnRememberMeChange(it)) }
        )

        CommunityTextButton(
          text = Res.string.auth_forgot_password_label,
          onClick = { onAction(LoginAction.OnForgotPasswordClick) }
        )
      }

      Spacer(modifier = Modifier.height(Spacing.extraLarge))

      CommunityButton(
        text = Res.string.auth_login_label,
        onClick = { onAction(LoginAction.OnLoginClick) },
        isLoading = state.isLoading
      )

      Spacer(modifier = Modifier.height(Spacing.medium))

      CommunityOutlinedButton(
        text = Res.string.auth_progress_without_account,
        onClick = { onAction(LoginAction.OnGuestClick) }
      )

      Spacer(modifier = Modifier.height(Spacing.large))

      Row(
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(stringResource(Res.string.auth_login_no_account_yet))
        CommunityTextButton(
          text = Res.string.auth_register_label,
          onClick = { onAction(LoginAction.OnRegisterClick) }
        )
      }
    }
  }
}