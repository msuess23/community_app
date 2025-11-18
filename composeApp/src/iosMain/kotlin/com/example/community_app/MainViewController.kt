package com.example.community_app

import androidx.compose.ui.window.ComposeUIViewController
import com.example.community_app.app.App
import com.example.community_app.di.initKoin

fun MainViewController() = ComposeUIViewController(
  configure = {
    initKoin()
  }
) { App() }