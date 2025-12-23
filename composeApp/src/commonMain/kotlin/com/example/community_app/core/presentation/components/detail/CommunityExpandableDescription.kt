package com.example.community_app.core.presentation.components.detail

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import compose.icons.FeatherIcons
import compose.icons.feathericons.ChevronDown
import compose.icons.feathericons.ChevronUp

@Composable
fun CommunityExpandableDescription(
  text: String,
  isExpanded: Boolean,
  onToggle: () -> Unit
) {
  var isExpandable by remember { mutableStateOf(false) }

  Column(modifier = Modifier.animateContentSize()) {
    Box(modifier = Modifier.fillMaxWidth()) {
      Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = if (isExpanded) Int.MAX_VALUE else 3,
        overflow = TextOverflow.Ellipsis,
        onTextLayout = { textLayoutResult ->
          if (textLayoutResult.lineCount > 3 || textLayoutResult.hasVisualOverflow) {
            isExpandable = true
          }
        },
        modifier = Modifier
          .fillMaxWidth()
          .clickable(enabled = isExpandable, onClick = onToggle)
      )

      if (isExpandable) {
        Box(
          modifier = Modifier
            .align(Alignment.BottomEnd)
            .background(
              Brush.horizontalGradient(
                0.0f to Color.Transparent,
                0.2f to MaterialTheme.colorScheme.surface,
                1.0f to MaterialTheme.colorScheme.surface
              )
            )
            .padding(start = 24.dp)
        ) {
          IconButton(
            onClick = onToggle,
            modifier = Modifier.size(24.dp)
          ) {
            Icon(
              imageVector = if (isExpanded) FeatherIcons.ChevronUp else FeatherIcons.ChevronDown,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(20.dp)
            )
          }
        }
      }
    }
  }
}