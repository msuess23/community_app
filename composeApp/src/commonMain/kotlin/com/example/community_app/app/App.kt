package com.example.community_app.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.example.community_app.app.navigation.AppScaffold
import com.example.community_app.app.navigation.Route
import com.example.community_app.auth.presentation.components.AuthGuard
import com.example.community_app.auth.presentation.login.LoginScreenRoot
import com.example.community_app.core.presentation.theme.CommunityTheme
import com.example.community_app.core.util.localeManager
import com.example.community_app.di.createKoinConfiguration
import com.example.community_app.info.presentation.info_detail.InfoDetailScreenRoot
import com.example.community_app.info.presentation.info_master.InfoMasterScreenRoot
import com.example.community_app.settings.domain.SettingsRepository
import com.example.community_app.settings.presentation.SettingsScreenRoot
import com.example.community_app.util.AppLanguage
import com.example.community_app.util.AppTheme
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.KoinMultiplatformApplication
import org.koin.compose.koinInject
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class)
@Composable
@Preview
fun App() {
  KoinMultiplatformApplication(
    config = createKoinConfiguration()
  ) {
    val settingsRepo = koinInject<SettingsRepository>()
    val settingsState by settingsRepo.settings.collectAsState(initial = null)

    LaunchedEffect(settingsState?.language) {
      settingsState?.language?.let { localeManager.applyLocale(it) }
    }

    val currentTheme = settingsState?.theme ?: AppTheme.SYSTEM
    val currentLanguage = settingsState?.language ?: AppLanguage.SYSTEM

    key(currentLanguage) {
      CommunityTheme(
        appTheme = currentTheme
      ) {
        val navController = rememberNavController()
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val scope = rememberCoroutineScope()

        AppScaffold(
          navController = navController,
          drawerState= drawerState
        ) {
          NavHost(
            navController = navController,
            startDestination = Route.InfoGraph
          ) {
            navigation<Route.AuthGraph>(
              startDestination = Route.Login
            ) {
              composable<Route.Login> {
                LoginScreenRoot(
                  onLoginSuccess = {
                    navController.navigate(Route.InfoGraph) {
                      popUpTo(Route.AuthGraph) { inclusive = true }
                    }
                  },
                  onNavigateToRegister = {
                    navController.navigate(Route.Register)
                  },
                  onNavigateToGuest = {
                    navController.navigate(Route.InfoGraph) {
                      popUpTo(Route.AuthGraph) { inclusive = true }
                    }
                  }
                )
              }

              composable<Route.Register> {
                DummyScreen(
                  title = "Registrieren",
                  onOpenDrawer = {}
                )
              }
            }

            navigation<Route.InfoGraph>(
              startDestination = Route.InfoMaster
            ) {
              composable<Route.InfoMaster> {
                InfoMasterScreenRoot(
                  onInfoClick = { info ->
                    navController.navigate(Route.InfoDetail(info.id))
                  },
                  onOpenDrawer = {
                    scope.launch { drawerState.open() }
                  }
                )
              }
              composable<Route.InfoDetail> { info ->
                InfoDetailScreenRoot(
                  onNavigateBack = { navController.popBackStack() }
                )
              }
            }

            navigation<Route.TicketGraph>(startDestination = Route.TicketMaster) {
              composable<Route.TicketMaster> {
                AuthGuard(
                  onLoginClick = { navController.navigate(Route.AuthGraph) }
                ) { user ->
                  DummyScreen(
                    title = "Ticket Master (Angemeldet als ${user.displayName})",
                    onOpenDrawer = { scope.launch { drawerState.open() } }
                  )
                }
              }
            }

            navigation<Route.OfficeGraph>(startDestination = Route.OfficeMaster) {
              composable<Route.OfficeMaster> {
                DummyScreen("Office Master", onOpenDrawer = { scope.launch { drawerState.open() } })
              }
            }

            navigation<Route.AppointmentGraph>(startDestination = Route.AppointmentMaster) {
              composable<Route.AppointmentMaster> {
                DummyScreen("Appointments Master", onOpenDrawer = { scope.launch { drawerState.open() } })
              }
            }

            composable<Route.Settings> {
              SettingsScreenRoot(onOpenDrawer = { scope.launch { drawerState.open() } })
            }
          }
        }
      }
    }
  }
}


@Composable
fun DummyScreen(title: String, onOpenDrawer: () -> Unit) {
  Column(
    modifier = Modifier.fillMaxSize(),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Text(title)
    Button(onClick = onOpenDrawer) {
      Text("Open Drawer")
    }
  }
}