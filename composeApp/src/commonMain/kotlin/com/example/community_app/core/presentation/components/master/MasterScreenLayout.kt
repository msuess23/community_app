package com.example.community_app.core.presentation.components.master

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import com.example.community_app.core.presentation.components.CommunityTopAppBar
import com.example.community_app.core.presentation.components.TopBarNavigationType
import com.example.community_app.core.presentation.components.list.ScreenMessage
import com.example.community_app.core.presentation.components.search.SearchBar
import com.example.community_app.core.presentation.theme.Spacing
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.filters_label
import community_app.composeapp.generated.resources.search_no_results
import compose.icons.FeatherIcons
import compose.icons.feathericons.Sliders
import org.jetbrains.compose.resources.stringResource

@Composable
fun MasterScreenLayout(
  searchQuery: String,
  isFilterActive: Boolean,
  isLoading: Boolean,
  isEmpty: Boolean,
  snackbarHostState: SnackbarHostState,

  onSearchQueryChange: (String) -> Unit,
  onRefresh: () -> Unit,
  onOpenDrawer: () -> Unit,
  onToggleFilterSheet: () -> Unit,

  modifier: Modifier = Modifier,
  floatingActionButton: @Composable () -> Unit = {},
  tabsContent: @Composable (ColumnScope.() -> Unit)? = null,
  emptyStateContent: @Composable () -> Unit = { DefaultEmptyState() },
  listContent: @Composable () -> Unit
) {
  val keyboardController = LocalSoftwareKeyboardController.current

  Scaffold(
    modifier = modifier,
    topBar = {
      CommunityTopAppBar(
        titleContent = {
          SearchBar(
            searchQuery = searchQuery,
            onSearchQueryChange = onSearchQueryChange,
            onImeSearch = { keyboardController?.hide() },
            modifier = Modifier.fillMaxWidth()
          )
        },
        navigationType = TopBarNavigationType.Drawer,
        onNavigationClick = onOpenDrawer,
        actions = {
          IconButton(onClick = onToggleFilterSheet) {
            val iconTint = if (isFilterActive)
              MaterialTheme.colorScheme.tertiaryContainer
            else
              MaterialTheme.colorScheme.onPrimary

            Icon(
              imageVector = FeatherIcons.Sliders,
              contentDescription = stringResource(Res.string.filters_label),
              tint = iconTint
            )
          }
        }
      )
    },
    floatingActionButton = floatingActionButton,
    snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .background(MaterialTheme.colorScheme.primary)
        .statusBarsPadding()
    ) {
      Surface(
        modifier = Modifier
          .weight(1f)
          .fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          if (tabsContent != null) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Spacing.medium, top = Spacing.extraSmall)
            ) {
              tabsContent()
            }
          }

          PullToRefreshBox(
            isRefreshing = isLoading,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
          ) {
            if (isLoading && isEmpty) {
              CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            } else if (isEmpty) {
              Box(
                modifier = Modifier
                  .fillMaxSize()
                  .verticalScroll(rememberScrollState()),
                contentAlignment = Alignment.Center
              ) {
                emptyStateContent()
              }
            } else {
              listContent()
            }
          }
        }
      }
    }
  }
}

@Composable
private fun DefaultEmptyState() {
  Box(
    modifier = Modifier.fillMaxSize(),
    contentAlignment = Alignment.Center
  ) {
    ScreenMessage(
      text = stringResource(Res.string.search_no_results),
      color = MaterialTheme.colorScheme.onSurface
    )
  }
}