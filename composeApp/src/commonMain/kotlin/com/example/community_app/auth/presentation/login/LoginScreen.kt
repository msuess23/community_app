package com.example.community_app.auth.presentation.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.auth_login_label
import community_app.composeapp.generated.resources.auth_register_label
import compose.icons.FeatherIcons
import compose.icons.feathericons.AtSign
import compose.icons.feathericons.Eye
import compose.icons.feathericons.EyeOff
import compose.icons.feathericons.Lock
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginScreenRoot(
  viewModel: LoginViewModel = koinViewModel(),
  onLoginSuccess: () -> Unit,
  onNavigateToRegister: () -> Unit,
  onNavigateToGuest: () -> Unit
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

  val errorMessage = state.errorMessage?.asString()
  LaunchedEffect(errorMessage) {
    if (errorMessage != null) snackbarHostState.showSnackbar(errorMessage)
  }

  Scaffold(
    snackbarHost = { SnackbarHost(snackbarHostState) }
  ) { padding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
          .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = "Willkommen",
          style = MaterialTheme.typography.headlineMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
          value = state.email,
          onValueChange = { onAction(LoginAction.OnEmailChange(it)) },
          label = { Text("E-Mail") },
          leadingIcon = { Icon(FeatherIcons.AtSign, null) },
          modifier = Modifier.fillMaxWidth(),
          keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
          ),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
          value = state.password,
          onValueChange = { onAction(LoginAction.OnPasswordChange(it)) },
          label = { Text("Passwort") },
          leadingIcon = { Icon(FeatherIcons.Lock, null) },
          trailingIcon = {
            IconButton(onClick = { onAction(LoginAction.OnTogglePasswordVisibility) }) {
              Icon(
                imageVector = if (state.isPasswordVisible) FeatherIcons.EyeOff else FeatherIcons.Eye,
                contentDescription = null
              )
            }
          },
          visualTransformation = if (state.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
          modifier = Modifier.fillMaxWidth(),
          keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
          ),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Checkbox(
            checked = state.isRememberMeChecked,
            onCheckedChange = { onAction(LoginAction.OnRememberMeChange(it)) }
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "angemeldet bleiben",
            style = MaterialTheme.typography.bodyMedium
          )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
          onClick = { onAction(LoginAction.OnLoginClick) },
          modifier = Modifier.fillMaxWidth().height(50.dp),
          enabled = !state.isLoading
        ) {
          if (state.isLoading) {
            CircularProgressIndicator()
          }
          Text(stringResource(Res.string.auth_login_label))
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
          onClick = { onAction(LoginAction.OnGuestClick) },
          modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
          Text("Ohne Anmeldung fortfahren")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text("Noch kein Konto?")
          TextButton(
            onClick = { onAction(LoginAction.OnRegisterClick) }
          ) {
            Text(stringResource(Res.string.auth_register_label))
          }
        }
      }
    }
  }
}