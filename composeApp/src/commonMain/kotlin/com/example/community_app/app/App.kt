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
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.example.community_app.app.navigation.AppScaffold
import com.example.community_app.app.navigation.Route
import com.example.community_app.app.navigation.TopLevelDestination
import com.example.community_app.appointment.presentation.detail.AppointmentDetailScreenRoot
import com.example.community_app.appointment.presentation.master.AppointmentMasterScreenRoot
import com.example.community_app.auth.presentation.components.AuthGuard
import com.example.community_app.auth.presentation.forgot_password.ForgotPasswordScreenRoot
import com.example.community_app.auth.presentation.login.LoginScreenRoot
import com.example.community_app.auth.presentation.register.RegisterScreenRoot
import com.example.community_app.auth.presentation.reset_password.ResetPasswordScreenRoot
import com.example.community_app.core.presentation.theme.CommunityTheme
import com.example.community_app.core.util.localeManager
import com.example.community_app.di.createKoinConfiguration
import com.example.community_app.info.presentation.info_detail.InfoDetailScreenRoot
import com.example.community_app.info.presentation.info_master.InfoMasterScreenRoot
import com.example.community_app.office.presentation.office_detail.OfficeDetailScreenRoot
import com.example.community_app.office.presentation.office_master.OfficeMasterScreenRoot
import com.example.community_app.profile.presentation.ProfileScreenRoot
import com.example.community_app.settings.domain.SettingsRepository
import com.example.community_app.settings.presentation.SettingsScreenRoot
import com.example.community_app.ticket.presentation.ticket_detail.TicketDetailScreenRoot
import com.example.community_app.ticket.presentation.ticket_edit.TicketEditScreenRoot
import com.example.community_app.ticket.presentation.ticket_master.TicketMasterScreenRoot
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.compose.BindEffect
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
    val permissionsController = koinInject<PermissionsController>()
    BindEffect(permissionsController)

    val settingsRepo = koinInject<SettingsRepository>()
    val settingsState by settingsRepo.settings.collectAsState(initial = null)

    LaunchedEffect(settingsState?.language) {
      settingsState?.language?.let { localeManager.applyLocale(it) }
    }

    if (settingsState == null) {
      AppLoadingScreen()
    } else {
      val currentTheme = settingsState!!.theme
      val currentLanguage = settingsState!!.language

      key(currentLanguage) {
        CommunityTheme(
          appTheme = currentTheme
        ) {
          val navController = rememberNavController()
          val drawerState = rememberDrawerState(DrawerValue.Closed)
          val scope = rememberCoroutineScope()

          val navBackStackEntry by navController.currentBackStackEntryAsState()
          val currentDestination = navBackStackEntry?.destination

          val showDrawer = TopLevelDestination.entries.any { destination ->
            currentDestination?.hierarchy?.any { it.hasRoute(destination.route::class) } == true
          }

          val showBottomBar = TopLevelDestination.entries.any { destination ->
            destination.showInBottomBar &&
                currentDestination?.hierarchy?.any { it.hasRoute(destination.route::class) } == true
          }

          AppScaffold(
            navController = navController,
            drawerState = drawerState,
            showBottomBar = showBottomBar,
            showDrawer = showDrawer
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
                    },
                    onNavigateToForgotPassword = {
                      navController.navigate(Route.ForgotPassword)
                    }
                  )
                }

                composable<Route.Register> {
                  RegisterScreenRoot(
                    onRegisterSuccess = {
                      navController.navigate(Route.InfoGraph) {
                        popUpTo(Route.AuthGraph) { inclusive = true }
                      }
                    },
                    onNavigateToLogin = {
                      navController.navigate(Route.Login)
                    },
                    onNavigateToGuest = {
                      navController.navigate(Route.InfoGraph) {
                        popUpTo(Route.AuthGraph) { inclusive = true }
                      }
                    }
                  )
                }

                composable<Route.ForgotPassword> {
                  ForgotPasswordScreenRoot(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToReset = { email ->
                      navController.navigate(Route.ResetPassword(email))
                    }
                  )
                }

                composable<Route.ResetPassword> {
                  ResetPasswordScreenRoot(
                    onSuccess = {
                      navController.navigate(Route.InfoGraph) {
                        popUpTo(Route.AuthGraph) { inclusive = true }
                      }
                    },
                    onNavigateBack = { navController.popBackStack() }
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
                  TicketMasterScreenRoot(
                    onNavigateToTicketDetail = { id, isDraft ->
                      navController.navigate(Route.TicketDetail(id, isDraft))
                    },
                    onNavigateToTicketEdit = { draftId ->
                      navController.navigate(Route.TicketEdit(draftId = draftId, ticketId = null))
                    },
                    onNavigateToLogin = { navController.navigate(Route.AuthGraph) },
                    onOpenDrawer = { scope.launch { drawerState.open() } }
                  )
                }

                composable<Route.TicketDetail> {
                  TicketDetailScreenRoot(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEdit = { ticketId, draftId ->
                      navController.navigate(Route.TicketEdit(ticketId = ticketId, draftId = draftId))
                    }
                  )
                }

                composable<Route.TicketEdit> {
                  TicketEditScreenRoot(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToMaster = { navController.popBackStack(Route.TicketMaster, inclusive = false) }
                  )
                }
              }

              navigation<Route.OfficeGraph>(startDestination = Route.OfficeMaster) {
                composable<Route.OfficeMaster> {
                  OfficeMasterScreenRoot(
                    onOfficeClick = { office ->
                      navController.navigate(Route.OfficeDetail(office.id))
                    },
                    onOpenDrawer = { scope.launch { drawerState.open() } }
                  )
                }

                composable<Route.OfficeDetail> {
                  OfficeDetailScreenRoot(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToLogin = { navController.navigate(Route.AuthGraph) }
                  )
                }
              }

              navigation<Route.AppointmentGraph>(startDestination = Route.AppointmentMaster) {
                composable<Route.AppointmentMaster> {
                  AppointmentMasterScreenRoot(
                    onNavigateToDetail = { appointmentId ->
                      navController.navigate(Route.AppointmentDetail(appointmentId)) },
                    onNavigateToLogin = { navController.navigate(Route.AuthGraph) },
                    onOpenDrawer = { scope.launch { drawerState.open() } }
                  )
                }

                composable<Route.AppointmentDetail> {
                  AppointmentDetailScreenRoot(
                    onNavigateBack = { navController.popBackStack() }
                  )
                }
              }

              composable<Route.Settings> {
                SettingsScreenRoot(
                  onOpenDrawer = { scope.launch { drawerState.open() } }
                )
              }

              composable<Route.Profile> {
                ProfileScreenRoot(
                  onOpenDrawer = { scope.launch { drawerState.open() } },
                  onNavigateToLogin = {
                    navController.navigate(Route.AuthGraph)
                  },
                  onNavigateToReset = { email ->
                    navController.navigate(Route.ResetPassword(email))
                  }
                )
              }
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