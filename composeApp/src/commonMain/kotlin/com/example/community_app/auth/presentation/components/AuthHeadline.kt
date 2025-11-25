package com.example.community_app.auth.presentation.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun AuthHeadline(
  text: StringResource
) {
  Text(
    text = stringResource(text),
    style = MaterialTheme.typography.headlineMedium,
    fontWeight = FontWeight.Bold,
    color = MaterialTheme.colorScheme.primary
  )
}