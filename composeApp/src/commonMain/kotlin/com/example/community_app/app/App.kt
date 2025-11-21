package com.example.community_app.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.community_app.core.presentation.CommunityTheme
import com.example.community_app.di.createKoinConfiguration
import com.example.community_app.info.presentation.info_detail.InfoDetailScreenRoot
import com.example.community_app.info.presentation.info_master.InfoMasterScreenRoot
import com.example.community_app.info.presentation.info_master.InfoMasterViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinMultiplatformApplication
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class)
@Composable
@Preview
fun App() {
  KoinMultiplatformApplication(
    config = createKoinConfiguration()
  ) {
    CommunityTheme {
      val navController = rememberNavController()
      NavHost(
        navController = navController,
        startDestination = Route.InfoGraph
      ) {
        navigation<Route.InfoGraph>(
          startDestination = Route.InfoMaster
        ) {
          composable<Route.InfoMaster> {
            InfoMasterScreenRoot(
              onInfoClick = { info ->
                navController.navigate(
                  Route.InfoDetail(info.id)
                )
              }
            )
          }
          composable<Route.InfoDetail> { info ->
            InfoDetailScreenRoot(
              onNavigateBack = { navController.popBackStack() }
            )
          }
        }
      }
    }
  }
}