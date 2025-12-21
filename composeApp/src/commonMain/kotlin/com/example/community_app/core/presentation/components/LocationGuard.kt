package com.example.community_app.core.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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

@Composable
fun <T> rememberLocationAwareItems(
  items: List<T>,
  requiresLocation: (T) -> Boolean
): List<T> {
  val location = LocalLocation.current
  return remember(items, location) {
    items.filter { item ->
      if (requiresLocation(item)) {
        location != null
      } else {
        true
      }
    }
  }
}