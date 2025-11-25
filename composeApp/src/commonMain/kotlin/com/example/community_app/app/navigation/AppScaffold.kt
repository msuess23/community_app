package com.example.community_app.app.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.community_app.core.presentation.theme.Size
import com.example.community_app.core.presentation.theme.Spacing
import com.example.community_app.util.AppLanguage
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.app_title
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun AppScaffold(
  navController: NavHostController,
  drawerState: DrawerState,
  showBottomBar: Boolean,
  showDrawer: Boolean,
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

  LaunchedEffect(showDrawer) {
    if (!showDrawer && drawerState.isOpen) drawerState.close()
  }

  val scaffoldContent = @Composable {
    Scaffold(
      bottomBar = {
        if (showBottomBar) {
          NavigationBar(
            containerColor = MaterialTheme.colorScheme.primary,
            windowInsets = WindowInsets(left = Spacing.medium, right = Spacing.medium, top = Spacing.extraSmall, bottom = Spacing.medium)
          ) {
            TopLevelDestination.entries.filter { it.showInBottomBar }.forEach { destination ->
              val selected = isSelected(destination)
              NavigationBarItem(
                selected = selected,
                onClick = { navigateTo(destination) },
                icon = {
                  Icon(
                    imageVector = if (selected) destination.selectedIcon else destination.unselectedIcon,
                    contentDescription = null,
                    modifier = Modifier.size(Size.iconMedium)
                  )
                },
                label = { Text(stringResource(destination.label)) },
                colors = NavigationBarItemDefaults.colors(
                  indicatorColor = MaterialTheme.colorScheme.onPrimary,
                  selectedIconColor = MaterialTheme.colorScheme.primary,
                  unselectedIconColor = MaterialTheme.colorScheme.onPrimary,
                  selectedTextColor = MaterialTheme.colorScheme.onPrimary,
                  unselectedTextColor = MaterialTheme.colorScheme.onPrimary
                )
              )
            }
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

  if (showDrawer) {
    ModalNavigationDrawer(
      drawerState = drawerState,
      gesturesEnabled = showDrawer,
      drawerContent = {
        if (showDrawer) {
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
      },
      content = scaffoldContent
    )
  } else {
    scaffoldContent()
  }
}