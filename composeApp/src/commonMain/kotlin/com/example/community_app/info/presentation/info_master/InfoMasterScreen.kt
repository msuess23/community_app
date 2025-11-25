package com.example.community_app.info.presentation.info_master

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.community_app.core.presentation.components.CommunityTopAppBar
import com.example.community_app.core.presentation.components.ObserveErrorMessage
import com.example.community_app.core.presentation.components.TopBarNavigationType
import com.example.community_app.core.presentation.components.list.CustomList
import com.example.community_app.core.presentation.components.list.ScreenMessage
import com.example.community_app.core.presentation.components.search.SearchBar
import com.example.community_app.info.domain.Info
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.filters_label
import community_app.composeapp.generated.resources.search_no_results
import compose.icons.FeatherIcons
import compose.icons.feathericons.Sliders
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel


@Composable
fun InfoMasterScreenRoot(
  viewModel: InfoMasterViewModel = koinViewModel(),
  onInfoClick: (Info) -> Unit,
  onOpenDrawer: () -> Unit
) {
  val state by viewModel.state.collectAsStateWithLifecycle()

  InfoMasterScreen(
    state = state,
    onAction = { action ->
      when(action) {
        is InfoMasterAction.OnInfoClick -> onInfoClick(action.info)
        else -> Unit
      }
      viewModel.onAction(action)
    },
    onOpenDrawer = onOpenDrawer
  )
}

@Composable
private fun InfoMasterScreen(
  state: InfoMasterState,
  onAction: (InfoMasterAction) -> Unit,
  onOpenDrawer: () -> Unit
) {
  val keyboardController = LocalSoftwareKeyboardController.current
  val lazyListState = rememberLazyListState()
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  LaunchedEffect(state.searchResults) {
    lazyListState.animateScrollToItem(0)
  }

  ObserveErrorMessage(
    errorMessage = state.errorMessage,
    snackbarHostState = snackbarHostState,
    isLoading = (state.isLoading && state.searchResults.isEmpty())
  )

  Scaffold(
    topBar = {
      CommunityTopAppBar(
        titleContent = {
          SearchBar(
            searchQuery = state.searchQuery,
            onSearchQueryChange = {
              onAction(InfoMasterAction.OnSearchQueryChange(it))
            },
            onImeSearch = {
              keyboardController?.hide()
            },
            modifier = Modifier.fillMaxWidth()
          )
        },
        navigationType = TopBarNavigationType.Drawer,
        onNavigationClick = onOpenDrawer,
        actions = {
          IconButton(
            onClick = { onAction(InfoMasterAction.OnToggleFilterSheet) }
          ) {
            val iconTint = if (state.filter.selectedCategories.isNotEmpty())
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
        shape = RoundedCornerShape(
          topStart = 32.dp,
          topEnd = 32.dp
        )
      ) {
        PullToRefreshBox(
          isRefreshing = state.isLoading,
          onRefresh = { onAction(InfoMasterAction.OnRefresh) },
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center
        ) {
          if (state.isLoading) {
            CircularProgressIndicator(
              color = MaterialTheme.colorScheme.primary
            )
          } else {
            when {
              state.errorMessage != null && state.searchResults.isEmpty() -> {
                ScreenMessage(
                  text = state.errorMessage.asString(),
                  color = MaterialTheme.colorScheme.error
                )
              }

              state.searchResults.isEmpty() && !state.isLoading -> {
                ScreenMessage(
                  text = stringResource(Res.string.search_no_results),
                  color = MaterialTheme.colorScheme.onSurface
                )
              }

              else -> {
                CustomList(
                  infos = state.searchResults,
                  onInfoClick = {
                    onAction(InfoMasterAction.OnInfoClick(it))
                  },
                  modifier = Modifier.fillMaxSize(),
                  scrollState = lazyListState
                )
              }
            }
          }
        }
      }
    }

    if (state.isFilterSheetVisible) {
      InfoFilterSheet(
        filterState = state.filter,
        onAction = onAction
      )
    }
  }
}