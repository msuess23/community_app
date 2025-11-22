package com.example.community_app.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.app_title
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun AppScaffold(
  navController: NavHostController,
  drawerState: DrawerState,
  content: @Composable () -> Unit
) {
  val scope = rememberCoroutineScope()
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentDestination = navBackStackEntry?.destination

  fun navigateTo(destination: TopLevelDestination) {
    navController.navigate(destination.route) {
      popUpTo(navController.graph.findStartDestination().id) {
        saveState = true
      }
      launchSingleTop = true
      restoreState = true
    }
    scope.launch { drawerState.close() }
  }

  fun isSelected(destination: TopLevelDestination): Boolean {
    return currentDestination?.hierarchy?.any { it.hasRoute(destination.route::class) } == true
  }

  ModalNavigationDrawer(
    drawerState = drawerState,
    drawerContent = {
      ModalDrawerSheet {
        Text(
          text = stringResource(Res.string.app_title),
          modifier = Modifier.padding(16.dp)
        )
        TopLevelDestination.entries.filter { it.showInDrawer }
          .forEach { destination ->
            NavigationDrawerItem(
              label = { Text(stringResource(destination.label)) },
              selected = isSelected(destination),
              onClick = { navigateTo(destination) },
              icon = {
                Icon(
                  imageVector = if (isSelected(destination)) destination.selectedIcon else destination.unselectedIcon,
                  contentDescription = stringResource(destination.label)
                )
              },
              modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
          }
      }
    }
  ) {
    Scaffold(
      bottomBar = {
        NavigationBar {
          TopLevelDestination.entries.filter { it.showInBottomBar }.forEach { destination ->
            val selected = isSelected(destination)
            NavigationBarItem(
              selected = selected,
              onClick = { navigateTo(destination) },
              icon = {
                Icon(
                  imageVector = if (selected) destination.selectedIcon else destination.unselectedIcon,
                  contentDescription = null
                )
              },
              label = { Text(stringResource(destination.label)) }
            )
          }
        }
      }
    ) { paddingValues ->
      Box(modifier = Modifier
        .padding(bottom = paddingValues.calculateBottomPadding())
      ) {
        content()
      }
    }
  }
}