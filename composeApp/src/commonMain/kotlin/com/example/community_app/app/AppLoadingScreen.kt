package com.example.community_app.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.community_app.core.presentation.theme.BluePrimary
import com.example.community_app.core.presentation.theme.WhitePure

@Composable
fun AppLoadingScreen() {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(BluePrimary),
    contentAlignment = Alignment.Center
  ) {
    CircularProgressIndicator(
      color = WhitePure
    )
  }
}