package com.example.community_app

import androidx.compose.runtime.*
import com.example.community_app.core.presentation.CommunityTheme
import com.example.community_app.info.presentation.info_master.InfoMasterScreenRoot
import com.example.community_app.info.presentation.info_master.InfoMasterViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Preview
fun App() {
  CommunityTheme {
    val viewModel = koinViewModel<InfoMasterViewModel>()
    InfoMasterScreenRoot(
      viewModel = viewModel,
      onInfoClick = {}
    )
  }
}