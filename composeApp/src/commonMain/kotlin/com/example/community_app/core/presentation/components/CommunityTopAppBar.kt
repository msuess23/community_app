package com.example.community_app.core.presentation.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.back
import community_app.composeapp.generated.resources.label_menu
import compose.icons.FeatherIcons
import compose.icons.feathericons.ChevronLeft
import compose.icons.feathericons.Menu
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityTopAppBar(
  modifier: Modifier = Modifier,
  titleContent: @Composable () -> Unit,
  navigationType: TopBarNavigationType = TopBarNavigationType.None,
  onNavigationClick: () -> Unit = {},
  actions: @Composable RowScope.() -> Unit = {},
  containerColor: Color = MaterialTheme.colorScheme.primary,
  contentColor: Color = MaterialTheme.colorScheme.onPrimary
  ) {
  TopAppBar(
    title = titleContent,
    modifier = modifier,
    navigationIcon = {
      when (navigationType) {
        TopBarNavigationType.Drawer -> {
          IconButton(onClick = onNavigationClick) {
            Icon(
              imageVector = FeatherIcons.Menu,
              contentDescription = stringResource(Res.string.label_menu),
              tint = contentColor
            )
          }
        }
        TopBarNavigationType.Back -> {
          IconButton(onClick = onNavigationClick) {
            Icon(
              imageVector = FeatherIcons.ChevronLeft,
              contentDescription = stringResource(Res.string.back),
              tint = contentColor
            )
          }
        }
        TopBarNavigationType.None -> {}
      }
    },
    actions = actions,
    colors = TopAppBarDefaults.topAppBarColors(
      containerColor = containerColor,
      titleContentColor = contentColor,
      navigationIconContentColor = contentColor,
      actionIconContentColor = contentColor
    )
  )
}

enum class TopBarNavigationType {
  Drawer, Back, None
}