package com.example.community_app.core.presentation.components.list

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.example.community_app.core.presentation.theme.Spacing

@Composable
fun ScreenMessage(
  text: String,
  color: Color
) {
  Text(
    text = text,
    textAlign = TextAlign.Center,
    style = MaterialTheme.typography.bodyLarge,
    color = color,
    modifier = Modifier.padding(Spacing.extraLarge)
  )
}