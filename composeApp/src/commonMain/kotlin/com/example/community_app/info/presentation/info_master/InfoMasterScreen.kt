package com.example.community_app.info.presentation.info_master

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.community_app.core.presentation.components.CommunityTopAppBar
import com.example.community_app.core.presentation.components.ObserveErrorMessage
import com.example.community_app.core.presentation.components.TopBarNavigationType
import com.example.community_app.core.presentation.components.list.InfoTicketListItem
import com.example.community_app.core.presentation.components.list.ScreenMessage
import com.example.community_app.core.presentation.components.search.SearchBar
import com.example.community_app.core.presentation.helpers.toUiText
import com.example.community_app.core.presentation.theme.Spacing
import com.example.community_app.core.util.formatIsoDate
import com.example.community_app.info.domain.Info
import com.example.community_app.ticket.presentation.ticket_master.TicketMasterAction
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.filters_label
import community_app.composeapp.generated.resources.info_plural
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

  val emptyScrollState = rememberScrollState()

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
        Column(
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          SecondaryTabRow(
            selectedTabIndex = 0,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.outlineVariant,
            divider = {
              HorizontalDivider(
                modifier = Modifier.height(1.dp),
                color = MaterialTheme.colorScheme.outlineVariant
              )
            },
            indicator = { },
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = Spacing.medium, top = Spacing.extraSmall)
          ) {
            Tab(
              selected = false,
              enabled = false,
              onClick = { },
              text = {
                Text(
                  text = stringResource(Res.string.info_plural),
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
              }
            )
          }

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
                state.searchResults.isEmpty() && !state.isLoading -> {
                  Box(
                    modifier = Modifier
                      .fillMaxSize()
                      .verticalScroll(emptyScrollState),
                    contentAlignment = Alignment.Center
                  ) {
                    ScreenMessage(
                      text = stringResource(Res.string.search_no_results),
                      color = MaterialTheme.colorScheme.onSurface
                    )
                  }
                }

                else -> {
                  LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = lazyListState,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                  ) {
                    items(
                      items = state.searchResults,
                      key = { it.id }
                    ) { info ->
                      val startDate = formatIsoDate(info.startsAt)
                      val endDate = formatIsoDate(info.endsAt)
                      val dateString = if (startDate == endDate) startDate else "$startDate - $endDate"

                      val categoryText = info.category.toUiText().asString()
                      val statusText = info.currentStatus?.toUiText()?.asString()
                      val sublineText = if (statusText != null) "$categoryText, $statusText" else categoryText

                      InfoTicketListItem(
                        title = info.title,
                        subtitle = sublineText,
                        dateString = dateString,
                        imageUrl = info.imageUrl,
                        onClick = { onAction(InfoMasterAction.OnInfoClick(info)) },
                        modifier = Modifier
                          .fillMaxWidth()
                          .padding(horizontal = 16.dp)
                      )
                    }
                  }
                }
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