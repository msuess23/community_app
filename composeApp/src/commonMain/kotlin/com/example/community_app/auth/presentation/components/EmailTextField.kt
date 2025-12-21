package com.example.community_app.auth.presentation.components

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.example.community_app.core.presentation.components.input.CommunityTextField
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.auth_email_label
import compose.icons.FeatherIcons
import compose.icons.feathericons.AtSign

@Composable
fun EmailTextField(
  value: String,
  onValueChange: (String) -> Unit,
  modifier: Modifier = Modifier,
  imeAction: ImeAction = ImeAction.Next
) {
  val focusManager = LocalFocusManager.current

  CommunityTextField(
    value = value,
    onValueChange = onValueChange,
    label = Res.string.auth_email_label,
    leadingIcon = { Icon(FeatherIcons.AtSign, null) },
    keyboardOptions = KeyboardOptions(
      keyboardType = KeyboardType.Email,
      imeAction = imeAction
    ),
    keyboardActions = KeyboardActions(
      onDone = { focusManager.clearFocus() }
    ),
    modifier = modifier
  )
}