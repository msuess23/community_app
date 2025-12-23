package com.example.community_app.core.presentation.components

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import com.example.community_app.core.presentation.helpers.UiText
import kotlinx.coroutines.launch

@Composable
fun ObserveErrorMessage(
  errorMessage: UiText?,
  snackbarHostState: SnackbarHostState,
  isLoading: Boolean = false,
  onErrorShown: () -> Unit = {}
) {
  val scope = rememberCoroutineScope()
  val messageText = errorMessage?.asString()

  LaunchedEffect(messageText, isLoading) {
    if (messageText != null && !isLoading) {
      scope.launch {
        snackbarHostState.showSnackbar(
          message = messageText,
          duration = SnackbarDuration.Short,
          withDismissAction = true
        )
        onErrorShown()
      }
    }
  }
}