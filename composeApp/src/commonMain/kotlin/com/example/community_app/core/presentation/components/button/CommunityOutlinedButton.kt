package com.example.community_app.core.presentation.components.button

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.example.community_app.core.presentation.theme.Size
import com.example.community_app.core.presentation.theme.Spacing
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun CommunityOutlinedButton(
  text: StringResource,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  icon: ImageVector? = null
) {
  OutlinedButton(
    onClick = onClick,
    modifier = modifier
      .fillMaxWidth()
      .height(Size.buttonHeight),
    enabled = enabled
  ) {
    if (icon != null) {
      Icon(
        imageVector = icon,
        contentDescription = null
      )
      Spacer(modifier = Modifier.width(Spacing.small))
    }
    Text(
      text = stringResource(text),
      style = MaterialTheme.typography.bodyLarge,
      fontWeight = FontWeight.SemiBold
    )
  }
}