package com.example.community_app.core.presentation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val LightColorScheme = lightColorScheme(
  // Primary
  primary = BluePrimary,
  onPrimary = BlueOnPrimary,
  primaryContainer = BlueLightContainer,
  onPrimaryContainer = BluePrimary,
  inversePrimary = DarkPrimary,

  // Secondary
  secondary = GraySecondary,
  onSecondary = BlueOnPrimary,
  secondaryContainer = GraySecondaryContainer,
  onSecondaryContainer = GraySecondary,

  // Tertiary
  tertiary = TealTertiary,
  onTertiary = BlueOnPrimary,
  tertiaryContainer = TealTertiaryContainer,
  onTertiaryContainer = TealTertiary,

  // Background & Surfaces
  background = WhitePure,
  onBackground = BlackPure,
  surface = WhitePure,
  onSurface = BlackPure,
  surfaceVariant = GrayVariant,
  onSurfaceVariant = GraySecondary,

  // Error
  error = LightError,
  onError = BlueOnPrimary,
  errorContainer = LightErrorContainer,
  onErrorContainer = LightError,

  // Elevation
  surfaceBright = LightSurfaceBright,
  surfaceDim = LightSurfaceDim,
  surfaceContainer = LightSurfaceContainer,
  surfaceContainerHigh = LightSurfaceContainerHigh,
  surfaceContainerHighest = LightSurfaceContainerHighest,
  surfaceContainerLow = LightSurfaceContainerLow,
  surfaceContainerLowest = LightSurfaceContainerLowest,

  // Else
  surfaceTint = BluePrimary,
  inverseSurface = DarkBackground,
  inverseOnSurface = WhitePure,
  outline = GrayOutline,
  outlineVariant = GrayVariant,
  scrim = Color(0xFF000000).copy(alpha = 0.5f)
)

val DarkColorScheme = darkColorScheme(
  // Primary
  primary = DarkPrimary,
  onPrimary = DarkOnPrimary,
  primaryContainer = DarkPrimaryContainer,
  onPrimaryContainer = DarkPrimary,
  inversePrimary = BluePrimary,

  // Secondary
  secondary = DarkSecondary,
  onSecondary = DarkSecondaryContainer,
  secondaryContainer = DarkSecondaryContainer,
  onSecondaryContainer = DarkSecondary,

  // Tertiary
  tertiary = DarkTertiary,
  onTertiary = DarkTertiaryContainer,
  tertiaryContainer = DarkTertiaryContainer,
  onTertiaryContainer = DarkTertiary,

  // Background & Surfaces
  background = DarkBackground,
  onBackground = DarkOnBackground,
  surface = DarkBackground,
  onSurface = DarkOnSurface,
  surfaceVariant = DarkOnSurface,
  onSurfaceVariant = DarkSecondary,

  // Error
  error = DarkError,
  onError = DarkErrorContainer,
  errorContainer = DarkErrorContainer,
  onErrorContainer = DarkError,

  // Elevation
  surfaceDim = DarkSurfaceDim,
  surfaceBright = DarkSurfaceBright,
  surfaceContainer = DarkSurfaceContainer,
  surfaceContainerHigh = DarkSurfaceContainerHigh,
  surfaceContainerHighest = DarkSurfaceContainerHighest,
  surfaceContainerLow = DarkSurfaceContainerLow,
  surfaceContainerLowest = DarkSurfaceContainerLowest,

  // Else
  surfaceTint = DarkPrimary,
  inverseSurface = WhitePure,
  inverseOnSurface = BlackPure,
  outline = DarkOutline,
  outlineVariant = DarkOnBackground,
  scrim = Color(0xFF000000).copy(alpha = 0.8f)
)

@Composable
fun CommunityTheme(
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = if(isSystemInDarkTheme()) DarkColorScheme else LightColorScheme,
    content = content
  )
}
