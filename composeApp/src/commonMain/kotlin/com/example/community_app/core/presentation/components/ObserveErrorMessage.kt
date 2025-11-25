package com.example.community_app.core.presentation.components

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.example.community_app.core.presentation.helpers.UiText

@Composable
fun ObserveErrorMessage(
  errorMessage: UiText?,
  snackbarHostState: SnackbarHostState,
  isLoading: Boolean = false,
  onErrorShown: () -> Unit = {}
) {
  val messageText = errorMessage?.asString()

  LaunchedEffect(messageText, isLoading) {
    if (messageText != null && !isLoading) {
      snackbarHostState.showSnackbar(messageText)
      onErrorShown()
    }
  }
}