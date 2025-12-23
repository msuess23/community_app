package com.example.community_app.core.presentation.composition_local

import androidx.compose.runtime.staticCompositionLocalOf
import com.example.community_app.core.domain.location.Location

val LocalLocation = staticCompositionLocalOf<Location?> { null }