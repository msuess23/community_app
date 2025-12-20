package com.example.community_app.core.presentation.components.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun CommunityDialog(
  title: StringResource,
  text: StringResource? = null,
  onDismissRequest: () -> Unit,
  confirmButtonText: StringResource,
  onConfirm: () -> Unit,
  modifier: Modifier = Modifier,
  dismissButtonText: StringResource? = null,
  onDismiss: (() -> Unit)? = null,
  icon: ImageVector? = null,
  content: @Composable (() -> Unit)? = null
) {
  AlertDialog(
    onDismissRequest = onDismissRequest,
    icon = icon?.let { { Icon(
      imageVector = it,
      contentDescription = null
    ) } },
    title = { stringResource(title) },
    text = {
      if (content != null) {
        content()
      } else if (text != null) {
        stringResource(text)
      }
    },
    modifier = modifier,
    confirmButton = {
      TextButton(onClick = onConfirm) {
        Text(stringResource(confirmButtonText))
      }
    },
    dismissButton = if (dismissButtonText != null) {
      {
        TextButton(onClick = { onDismiss?.invoke() ?: onDismissRequest() }) {
          Text(stringResource(dismissButtonText))
        }
      }
    } else null
  )
}