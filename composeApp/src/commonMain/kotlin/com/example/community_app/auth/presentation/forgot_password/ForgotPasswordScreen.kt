package com.example.community_app.auth.presentation.forgot_password

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.community_app.auth.presentation.components.EmailTextField
import com.example.community_app.core.presentation.components.CommunityTopAppBar
import com.example.community_app.core.presentation.components.ObserveErrorMessage
import com.example.community_app.core.presentation.components.TopBarNavigationType
import com.example.community_app.core.presentation.components.button.CommunityButton
import com.example.community_app.core.presentation.components.dialog.CommunityDialog
import com.example.community_app.core.presentation.theme.Spacing
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.auth_forgot_password_dialog_text
import community_app.composeapp.generated.resources.auth_forgot_password_dialog_title
import community_app.composeapp.generated.resources.auth_forgot_password_label
import community_app.composeapp.generated.resources.auth_forgot_password_request_code
import community_app.composeapp.generated.resources.auth_forgot_password_text
import community_app.composeapp.generated.resources.next
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ForgotPasswordScreenRoot(
  viewModel: ForgotPasswordViewModel = koinViewModel(),
  onNavigateBack: () -> Unit,
  onNavigateToReset: (String) -> Unit
) {
  val state by viewModel.state.collectAsStateWithLifecycle()

  ForgotPasswordScreen(
    state = state,
    onAction = { action ->
      when(action) {
        ForgotPasswordAction.OnNavigateBack -> onNavigateBack()
        ForgotPasswordAction.OnDialogDismiss -> {
          viewModel.onAction(ForgotPasswordAction.OnDialogDismiss)
          onNavigateToReset(state.email)
        }
        else -> viewModel.onAction(action)
      }
    }
  )
}

@Composable
private fun ForgotPasswordScreen(
  state: ForgotPasswordState,
  onAction: (ForgotPasswordAction) -> Unit
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
        titleContent = { Text(stringResource(Res.string.auth_forgot_password_label)) },
        navigationType = TopBarNavigationType.Back,
        onNavigationClick = { onAction(ForgotPasswordAction.OnNavigateBack) }
      )
    },
    snackbarHost = { SnackbarHost(snackbarHostState) }
  ) { padding ->
    Column(
      modifier = Modifier
        .padding(padding)
        .fillMaxSize()
        .padding(Spacing.screenPadding),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Text(
        text = stringResource(Res.string.auth_forgot_password_text),
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center
      )
      Spacer(Modifier.height(Spacing.blockSpacing))

      EmailTextField(
        value = state.email,
        onValueChange = { onAction(ForgotPasswordAction.OnEmailChange(it)) },
      )
      Spacer(Modifier.height(Spacing.extraLarge))

      CommunityButton(
        text = Res.string.auth_forgot_password_request_code,
        onClick = { onAction(ForgotPasswordAction.OnSubmitClick) },
        enabled = !state.isLoading
      )
    }
  }

  if (state.showSuccessDialog) {
    CommunityDialog(
      title = Res.string.auth_forgot_password_dialog_title,
      text = Res.string.auth_forgot_password_dialog_text,
      onDismissRequest = {},
      confirmButtonText = Res.string.next,
      onConfirm = { onAction(ForgotPasswordAction.OnDialogDismiss) }
    )
  }
}