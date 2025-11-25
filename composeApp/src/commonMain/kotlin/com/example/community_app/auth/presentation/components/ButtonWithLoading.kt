package com.example.community_app.auth.presentation.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun ButtonWithLoading(
  label: StringResource,
  onClick: () -> Unit,
  modifier: Modifier = Modifier.fillMaxWidth().height(50.dp),
  enabled: Boolean
) {
  Button(
    onClick = onClick,
    modifier = modifier,
    enabled = enabled
  ) {
    if (!enabled) {
      CircularProgressIndicator(
        color = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier.size(24.dp),
        strokeWidth = 2.dp
      )
      Spacer(Modifier.width(8.dp))
    }
    Text(stringResource(label))
  }
}