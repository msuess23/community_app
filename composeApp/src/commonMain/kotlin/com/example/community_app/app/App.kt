package com.example.community_app.app

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.example.community_app.core.presentation.theme.CommunityTheme
import com.example.community_app.di.createKoinConfiguration
import com.example.community_app.info.presentation.info_detail.InfoDetailScreenRoot
import com.example.community_app.info.presentation.info_master.InfoMasterScreenRoot
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinMultiplatformApplication
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