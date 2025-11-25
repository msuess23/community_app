package com.example.community_app.auth.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.auth_email_label
import compose.icons.FeatherIcons
import compose.icons.feathericons.AtSign
import org.jetbrains.compose.resources.stringResource

@Composable
fun EmailTextField(
  value: String,
  onValueChange: (String) -> Unit,
  modifier: Modifier = Modifier.fillMaxWidth(),
  imeAction: ImeAction = ImeAction.Next
) {
  OutlinedTextField(
    value = value,
    onValueChange = onValueChange,
    label = { Text(stringResource(Res.string.auth_email_label)) },
    leadingIcon = { Icon(FeatherIcons.AtSign, null) },
    modifier = modifier,
    keyboardOptions = KeyboardOptions(
      keyboardType = KeyboardType.Email,
      imeAction = imeAction
    ),
    singleLine = true
  )
}