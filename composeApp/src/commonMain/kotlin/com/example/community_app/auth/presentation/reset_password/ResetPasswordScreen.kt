package com.example.community_app.auth.presentation.reset_password

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.community_app.auth.presentation.components.ButtonWithLoading
import com.example.community_app.auth.presentation.components.PasswordTextField
import com.example.community_app.core.presentation.components.CommunityTopAppBar
import com.example.community_app.core.presentation.components.TopBarNavigationType
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
  val errorMsg = state.errorMessage?.asString()
  LaunchedEffect(errorMsg) {
    if (errorMsg != null) snackbarHostState.showSnackbar(errorMsg)
  }

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
        .padding(24.dp),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      OutlinedTextField(
        value = state.otp,
        onValueChange = { onAction(ResetPasswordAction.OnOtpChange(it)) },
        label = { Text(stringResource(Res.string.auth_otp_label)) },
        leadingIcon = { Icon(FeatherIcons.Hash, null) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(
          keyboardType = KeyboardType.NumberPassword,
          imeAction = ImeAction.Next
        )
      )

      Spacer(modifier = Modifier.height(16.dp))

      PasswordTextField(
        label = Res.string.auth_new_password_label,
        value = state.password,
        onValueChange = { onAction(ResetPasswordAction.OnPasswordChange(it)) },
        isPasswordVisible = state.isPasswordVisible,
        onTogglePasswordVisibility = { onAction(ResetPasswordAction.OnTogglePasswordVisibility) },
        imeAction = ImeAction.Next
      )

      Spacer(modifier = Modifier.height(16.dp))

      PasswordTextField(
        label = Res.string.auth_new_password_repeat_label,
        value = state.passwordRepeat,
        onValueChange = { onAction(ResetPasswordAction.OnPasswordRepeatChange(it)) },
        isPasswordVisible = state.isPasswordVisible,
        onTogglePasswordVisibility = { onAction(ResetPasswordAction.OnTogglePasswordVisibility) }
      )

      Spacer(modifier = Modifier.height(24.dp))

      ButtonWithLoading(
        label = Res.string.auth_reset_password_label,
        onClick = { onAction(ResetPasswordAction.OnSubmit) },
        enabled = !state.isLoading
      )
    }

    if (state.showSuccessDialog) {
      AlertDialog(
        onDismissRequest = { },
        title = { Text(stringResource(Res.string.auth_reset_password_dialog_title)) },
        text = { Text(stringResource(Res.string.auth_reset_password_dialog_text)) },
        confirmButton = {
          TextButton(onClick = { onAction(ResetPasswordAction.OnSuccessConfirm) }) {
            Text(stringResource(Res.string.next))
          }
        }
      )
    }
  }
}