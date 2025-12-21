package com.example.community_app.core.presentation.components

import androidx.compose.runtime.Composable
import com.example.community_app.core.domain.location.Location
import com.example.community_app.core.presentation.composition_local.LocalLocation

@Composable
fun LocationGuard(
  fallback: @Composable () -> Unit = {},
  content: @Composable (Location) -> Unit
) {
  val location = LocalLocation.current

  if (location != null) {
    content(location)
  } else {
    fallback()
  }
}