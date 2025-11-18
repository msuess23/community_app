package com.example.community_app.core.presentation

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DarkColorScheme = darkColorScheme(
  surfaceContainerLowest = Color.Red,
  surfaceContainerLow = Color.Gray,
  surfaceContainer = Color.DarkGray,
  surfaceContainerHigh = Color.Blue,
  outline = Color.Cyan,
  primary = Color.Black, // Button
  secondary = Color.Green,
  onSurface = Color.Magenta,
  primaryContainer = Color.Yellow, // Screen background
  error = Color.LightGray,
  onPrimary = Color.White // Button Text
)

val LightColorScheme = lightColorScheme(
  primaryContainer = Color.White,
  primary = Color.Blue,
  onPrimary = Color.White
)

@Composable
fun CommunityTheme(
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = LightColorScheme,
    content = content
  )
}
