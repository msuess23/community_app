package com.example.community_app.core.presentation.components.input

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun CommunityTextField(
  value: String,
  onValueChange: (String) -> Unit,
  label: StringResource,
  modifier: Modifier = Modifier,
  leadingIcon: @Composable (() -> Unit)? = null,
  trailingIcon: @Composable (() -> Unit)? = null,
  isError: Boolean = false,
  errorMessage: String? = null,
  visualTransformation: VisualTransformation = VisualTransformation.None,
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
  keyboardActions: KeyboardActions = KeyboardActions.Default,
  singleLine: Boolean = true,
  enabled: Boolean = true
) {
  OutlinedTextField(
    value = value,
    onValueChange = onValueChange,
    label = { Text(stringResource(label)) },
    modifier = modifier.fillMaxWidth(),
    leadingIcon = leadingIcon,
    trailingIcon = trailingIcon,
    isError = isError,
    supportingText = if (errorMessage != null) {
      { Text(errorMessage) }
    } else null,
    visualTransformation = visualTransformation,
    keyboardOptions = keyboardOptions,
    keyboardActions = keyboardActions,
    singleLine = singleLine,
    enabled = enabled
  )
}