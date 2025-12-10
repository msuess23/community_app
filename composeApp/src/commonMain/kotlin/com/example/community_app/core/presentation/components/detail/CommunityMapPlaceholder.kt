package com.example.community_app.core.presentation.components.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MapPlaceholder() {
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .height(180.dp)
      .background(MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.medium),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = "MapPlaceholder",
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}