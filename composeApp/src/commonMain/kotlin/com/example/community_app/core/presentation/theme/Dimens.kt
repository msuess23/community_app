package com.example.community_app.core.presentation.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.dp

object Spacing {
  val default = 0.dp
  val extraSmall = 4.dp
  val small = 8.dp
  val medium = 16.dp
  val large = 24.dp
  val extraLarge = 32.dp

  val screenPadding = large
  val listContentPadding = medium
  val itemSpacing = medium
  val blockSpacing = large

  val listPadding = PaddingValues(top = Spacing.medium, bottom = Spacing.large)
}

object Size {
  val iconSmall = 18.dp
  val iconMedium = 24.dp
  val iconLarge = 36.dp
  val iconExtraLarge = 64.dp

  val buttonHeight = 50.dp
  val thumbnailHeight = 100.dp
}