package com.example.community_app

import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import com.example.community_app.di.initKoin
import io.ktor.client.engine.darwin.Darwin

fun MainViewController() = ComposeUIViewController(
  configure = {
    initKoin()
  }
) { App() }