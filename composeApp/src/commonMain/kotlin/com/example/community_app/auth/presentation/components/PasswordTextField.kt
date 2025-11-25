package com.example.community_app.auth.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.auth_password_label
import compose.icons.FeatherIcons
import compose.icons.feathericons.Eye
import compose.icons.feathericons.EyeOff
import compose.icons.feathericons.Lock
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun PasswordTextField(
  value: String,
  onValueChange: (String) -> Unit,
  isPasswordVisible: Boolean,
  onTogglePasswordVisibility: () -> Unit,
  modifier: Modifier = Modifier.fillMaxWidth(),
  label: StringResource = Res.string.auth_password_label,
  imeAction: ImeAction = ImeAction.Done
) {
  OutlinedTextField(
    value = value,
    onValueChange = onValueChange,
    label = { Text(stringResource(label)) },
    leadingIcon = { Icon(FeatherIcons.Lock, null) },
    trailingIcon = {
      IconButton(onClick = onTogglePasswordVisibility) {
        Icon(
          imageVector = if (isPasswordVisible) FeatherIcons.EyeOff else FeatherIcons.Eye,
          contentDescription = null
        )
      }
    },
    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
    modifier = modifier,
    keyboardOptions = KeyboardOptions(
      keyboardType = KeyboardType.Password,
      imeAction = imeAction
    ),
    singleLine = true
  )
}