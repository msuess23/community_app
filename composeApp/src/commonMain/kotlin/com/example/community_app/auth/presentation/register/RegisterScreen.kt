package com.example.community_app.auth.presentation.register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.community_app.auth.presentation.components.ButtonWithLoading
import com.example.community_app.auth.presentation.components.CommunityCheckbox
import com.example.community_app.auth.presentation.components.EmailTextField
import com.example.community_app.auth.presentation.components.PasswordTextField
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.auth_already_has_account
import community_app.composeapp.generated.resources.auth_login_label
import community_app.composeapp.generated.resources.auth_name_label
import community_app.composeapp.generated.resources.auth_no_account_yet
import community_app.composeapp.generated.resources.auth_progress_without_account
import community_app.composeapp.generated.resources.auth_register_label
import community_app.composeapp.generated.resources.auth_register_title
import community_app.composeapp.generated.resources.auth_remember_me
import community_app.composeapp.generated.resources.password_confirm_label
import community_app.composeapp.generated.resources.welcome
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

  val errorMessage = state.errorMessage?.asString()
  LaunchedEffect(errorMessage) {
    if (errorMessage != null) snackbarHostState.showSnackbar(errorMessage)
  }

  Scaffold(
    snackbarHost = { SnackbarHost(snackbarHostState) }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .verticalScroll(rememberScrollState())
        .padding(24.dp),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = stringResource(Res.string.auth_register_title),
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
      )

      Spacer(modifier = Modifier.height(32.dp))

      OutlinedTextField(
        value = state.displayName,
        onValueChange = { onAction(RegisterAction.OnDisplayNameChange(it)) },
        label = { Text(stringResource(Res.string.auth_name_label)) },
        leadingIcon = { Icon(FeatherIcons.User, null) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
      )

      Spacer(modifier = Modifier.height(16.dp))

      EmailTextField(
        value = state.email,
        onValueChange = { onAction(RegisterAction.OnEmailChange(it)) }
      )

      Spacer(modifier = Modifier.height(16.dp))

      PasswordTextField(
        value = state.password,
        onValueChange = { onAction(RegisterAction.OnPasswordChange(it)) },
        isPasswordVisible = state.isPasswordVisible,
        onTogglePasswordVisibility = { onAction(RegisterAction.OnTogglePasswordVisibility) },
        imeAction = ImeAction.Next
      )

      Spacer(modifier = Modifier.height(16.dp))

      PasswordTextField(
        label = Res.string.password_confirm_label,
        value = state.passwordRepeat,
        onValueChange = { onAction(RegisterAction.OnPasswordRepeatChange(it)) },
        isPasswordVisible = state.isPasswordVisible,
        onTogglePasswordVisibility = { onAction(RegisterAction.OnTogglePasswordVisibility) }
      )

      Spacer(modifier = Modifier.height(24.dp))

      ButtonWithLoading(
        label = Res.string.auth_register_label,
        onClick = { onAction(RegisterAction.OnRegisterClick) },
        enabled = !state.isLoading
      )

      Spacer(modifier = Modifier.height(16.dp))

      OutlinedButton(
        onClick = { onAction(RegisterAction.OnGuestClick) },
        modifier = Modifier.fillMaxWidth().height(50.dp)
      ) {
        Text(stringResource(Res.string.auth_progress_without_account))
      }

      Spacer(modifier = Modifier.height(24.dp))

      Row(
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(stringResource(Res.string.auth_already_has_account))
        TextButton(
          onClick = { onAction(RegisterAction.OnLoginClick) }
        ) {
          Text(stringResource(Res.string.auth_login_label))
        }
      }
    }
  }
}