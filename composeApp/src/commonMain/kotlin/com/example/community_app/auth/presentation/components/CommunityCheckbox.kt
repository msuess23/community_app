package com.example.community_app.auth.presentation.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun CommunityCheckbox(
  label: StringResource,
  modifier: Modifier = Modifier,
  checked: Boolean = false,
  onCheckChange: (Boolean) -> Unit
) {
  Row(
    modifier = modifier,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Checkbox(
      checked = checked,
      onCheckedChange = onCheckChange
    )
    Spacer(modifier = Modifier.width(8.dp))
    Text(
      text = stringResource(label),
      style = MaterialTheme.typography.bodyMedium
    )
  }
}