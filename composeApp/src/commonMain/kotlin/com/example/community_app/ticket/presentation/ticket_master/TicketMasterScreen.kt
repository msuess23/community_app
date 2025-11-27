package com.example.community_app.ticket.presentation.ticket_master

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
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
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.filters_label
import community_app.composeapp.generated.resources.search_no_results
import compose.icons.FeatherIcons
import compose.icons.feathericons.Plus
import compose.icons.feathericons.Sliders
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel


@Composable
fun TicketMasterScreenRoot(
  viewModel: TicketMasterViewModel = koinViewModel(),
  onOpenDrawer: () -> Unit,
  onNavigateToTicketDetail: (Long, Boolean) -> Unit,
  onNavigateToTicketEdit: (Long?) -> Unit
) {
  val state by viewModel.state.collectAsStateWithLifecycle()

  TicketMasterScreen(
    state = state,
    onAction = { action ->
      when (action) {
        is TicketMasterAction.OnCreateTicketClick -> onNavigateToTicketEdit(null)
        is TicketMasterAction.OnTicketClick -> onNavigateToTicketDetail(action.ticket.id.toLong(), false)
        is TicketMasterAction.OnDraftClick -> onNavigateToTicketDetail(action.draft.id, true)
        else -> viewModel.onAction(action)
      }
    },
    onOpenDrawer = onOpenDrawer
  )
}

@Composable
private fun TicketMasterScreen(
  state: TicketMasterState,
  onAction: (TicketMasterAction) -> Unit,
  onOpenDrawer: () -> Unit
) {
  val keyboardController = LocalSoftwareKeyboardController.current
  val snackbarHostState = remember { SnackbarHostState() }
  val tabs = listOf("Community", "Meins") // TODO: localize

  val currentList = if (state.selectedTabIndex == 0) {
    state.communitySearchResults
  } else {
    state.userSearchResults
  }

  val lazyListState = rememberLazyListState()
  val emptyScrollState = rememberScrollState()

  LaunchedEffect(state.selectedTabIndex) {
    lazyListState.animateScrollToItem(0)
  }

  ObserveErrorMessage(
    errorMessage = state.errorMessage,
    snackbarHostState = snackbarHostState,
    isLoading = state.isLoading
  )

  Scaffold(
    topBar = {
      CommunityTopAppBar(
        titleContent = {
          SearchBar(
            searchQuery = state.searchQuery,
            onSearchQueryChange = {
              onAction(TicketMasterAction.OnSearchQueryChange(it))
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
            onClick = { onAction(TicketMasterAction.OnToggleFilterSheet) }
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
    floatingActionButton = {
      if (state.isUserLoggedIn) {
        FloatingActionButton(
          onClick = { onAction(TicketMasterAction.OnCreateTicketClick) },
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
          Icon(imageVector = FeatherIcons.Plus, contentDescription = null)
        }
      }
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
            selectedTabIndex = state.selectedTabIndex,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            divider = { HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant) },
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = Spacing.medium)
          ) {
            tabs.forEachIndexed { index, title ->
              Tab(
                selected = state.selectedTabIndex == index,
                onClick = { onAction(TicketMasterAction.OnTabChange(index)) },
                text = {
                  Text(
                    text = title,
                    fontWeight = if (state.selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                  )
                }
              )
            }
          }

          PullToRefreshBox(
            isRefreshing = state.isLoading,
            onRefresh = { onAction(TicketMasterAction.OnRefresh) },
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
          ) {
            when {
              state.isLoading -> {
                CircularProgressIndicator(
                  color = MaterialTheme.colorScheme.primary
                )
              }

              currentList.isEmpty() && !state.isLoading -> {
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
                  items(currentList) { item ->
                    val (title, subtitle, date, image, isDraft) = when(item) {
                      is TicketUiItem.Remote -> {
                        val cat = item.ticket.category.toUiText().asString()
                        val status = item.ticket.currentStatus?.toUiText()?.asString()
                        val sub = if(status != null) "$cat, $status" else cat
                        TicketItemData(
                          item.ticket.title,
                          sub,
                          formatIsoDate(item.ticket.createdAt),
                          item.ticket.imageUrl,
                          false
                        )
                      }
                      is TicketUiItem.Local -> {
                        val cat = item.draft.category?.toUiText()?.asString() ?: "-"
                        TicketItemData(
                          item.draft.title.ifBlank { "Neues Anliegen (Entwurf)" },
                          cat,
                          "Entwurf vom ${formatIsoDate(item.draft.lastModified)}",
                          item.draft.images.firstOrNull(),
                          true
                        )
                      }
                    }

                    InfoTicketListItem(
                      title = title,
                      subtitle = subtitle,
                      dateString = date,
                      imageUrl = image,
                      isDraft = isDraft,
                      onClick = {
                        when (item) {
                          is TicketUiItem.Remote -> onAction(TicketMasterAction.OnTicketClick(item.ticket))
                          is TicketUiItem.Local -> onAction(TicketMasterAction.OnDraftClick(item.draft))
                        }
                      },
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
      if (state.isFilterSheetVisible) {
        TicketFilterSheet(
          filterState = state.filter,
          isCommunityTab = state.selectedTabIndex == 0,
          onAction = onAction
        )
      }
    }
  }
}

private data class TicketItemData(
  val title: String,
  val subtitle: String,
  val date: String,
  val image: String?,
  val isDraft: Boolean
)