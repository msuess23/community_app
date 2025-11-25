package com.example.community_app.core.presentation.components.button

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun CommunityTextButton(
  text: StringResource,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  color: Color = MaterialTheme.colorScheme.primary
) {
  TextButton(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled
  ) {
    Text(
      text = stringResource(text),
      style = MaterialTheme.typography.bodyLarge,
      fontWeight = FontWeight.SemiBold,
      color = color
    )
  }
}