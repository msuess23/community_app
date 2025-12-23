package com.example.community_app.ticket.presentation.ticket_master

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.community_app.auth.presentation.components.AuthGuard
import com.example.community_app.core.presentation.components.ObserveErrorMessage
import com.example.community_app.core.presentation.components.list.InfoTicketListItem
import com.example.community_app.core.presentation.components.list.ScreenMessage
import com.example.community_app.core.presentation.components.master.MasterScreenLayout
import com.example.community_app.core.presentation.helpers.toUiText
import com.example.community_app.core.presentation.theme.Spacing
import com.example.community_app.core.util.formatIsoDate
import com.example.community_app.core.util.formatMillisDate
import com.example.community_app.ticket.domain.model.TicketListItem
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.create
import community_app.composeapp.generated.resources.search_no_results
import community_app.composeapp.generated.resources.ticket_ownership_community
import community_app.composeapp.generated.resources.ticket_ownership_user
import compose.icons.FeatherIcons
import compose.icons.feathericons.Plus
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel


@Composable
fun TicketMasterScreenRoot(
  viewModel: TicketMasterViewModel = koinViewModel(),
  onOpenDrawer: () -> Unit,
  onNavigateToTicketDetail: (Long, Boolean) -> Unit,
  onNavigateToTicketEdit: (Long?) -> Unit,
  onNavigateToLogin: () -> Unit
) {
  val state by viewModel.state.collectAsStateWithLifecycle()

  TicketMasterScreen(
    state = state,
    onAction = { action ->
      when (action) {
        is TicketMasterAction.OnCreateTicketClick -> onNavigateToTicketEdit(null)
        is TicketMasterAction.OnTicketClick -> onNavigateToTicketDetail(action.ticket.id.toLong(), false)
        is TicketMasterAction.OnDraftClick -> onNavigateToTicketDetail(action.draft.id, true)
        is TicketMasterAction.OnLoginClick -> onNavigateToLogin()
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
  val snackbarHostState = remember { SnackbarHostState() }
  val tabs = listOf(Res.string.ticket_ownership_community, Res.string.ticket_ownership_user)

  val currentList = if (state.selectedTabIndex == 0) {
    state.communitySearchResults
  } else {
    state.userSearchResults
  }

  val lazyListState = rememberLazyListState()

  LaunchedEffect(state.selectedTabIndex) {
    lazyListState.animateScrollToItem(0)
  }

  ObserveErrorMessage(
    errorMessage = state.errorMessage,
    snackbarHostState = snackbarHostState,
    isLoading = state.isLoading
  )

  MasterScreenLayout(
    searchQuery = state.searchQuery,
    isFilterActive = state.filter.selectedCategories.isNotEmpty() || state.filter.selectedStatuses.isNotEmpty(),
    isLoading = state.isLoading,
    isEmpty = currentList.isEmpty(),
    snackbarHostState = snackbarHostState,
    onSearchQueryChange = { onAction(TicketMasterAction.OnSearchQueryChange(it)) },
    onRefresh = { onAction(TicketMasterAction.OnRefresh) },
    onOpenDrawer = onOpenDrawer,
    onToggleFilterSheet = { onAction(TicketMasterAction.OnToggleFilterSheet) },
    floatingActionButton = {
      AuthGuard(
        fallbackContent = {}
      ) {
        FloatingActionButton(
          onClick = { onAction(TicketMasterAction.OnCreateTicketClick) },
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
          Icon(
            imageVector = FeatherIcons.Plus,
            contentDescription = stringResource(Res.string.create)
          )
        }
      }
    },
    tabsContent = {
      SecondaryTabRow(
        selectedTabIndex = state.selectedTabIndex,
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.primary,
        divider = { HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant) }
      ) {
        tabs.forEachIndexed { index, title ->
          Tab(
            selected = state.selectedTabIndex == index,
            onClick = { onAction(TicketMasterAction.OnTabChange(index)) },
            text = {
              Text(
                text = stringResource(title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (state.selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.primary
              )
            }
          )
        }
      }
    },
    emptyStateContent = {
      if (state.selectedTabIndex == 1) {
        AuthGuard(
          onLoginClick = { onAction(TicketMasterAction.OnLoginClick) }
        ) {
          ScreenMessage(
            text = stringResource(Res.string.search_no_results),
            color = MaterialTheme.colorScheme.onSurface
          )
        }
      }
    }
  ) {
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = Spacing.listPadding,
      state = lazyListState,
      verticalArrangement = Arrangement.spacedBy(12.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      items(currentList) { item ->
        val (title, subtitle, date, image, isDraft, isFavorite, isVoted) = when (item) {
          is TicketListItem.Remote -> {
            val cat = item.ticket.category.toUiText().asString()
            val status = item.ticket.currentStatus?.toUiText()?.asString()
            val sub = if (status != null) "$cat, $status" else cat
            TicketItemData(
              title = item.ticket.title,
              subtitle = sub,
              date = formatIsoDate(item.ticket.createdAt),
              image = item.ticket.imageUrl,
              isDraft = false,
              isFavorite = item.ticket.isFavorite,
              isVoted = item.ticket.userVoted == true
            )
          }

          is TicketListItem.Local -> {
            val cat = item.draft.category?.toUiText()?.asString() ?: "-"
            val dateStr = try {
              formatMillisDate(item.draft.lastModified.toLong())
            } catch (e: Exception) {
              "-"
            }

            TicketItemData(
              title = item.draft.title,
              subtitle = cat,
              date = dateStr,
              image = item.draft.images.firstOrNull(),
              isDraft = true,
              isFavorite = false,
              isVoted = false
            )
          }
        }

        InfoTicketListItem(
          title = title,
          subtitle = subtitle,
          dateString = date,
          imageUrl = image,
          isDraft = isDraft,
          isFavorite = isFavorite,
          isVoted = isVoted,
          onClick = {
            when (item) {
              is TicketListItem.Remote -> onAction(TicketMasterAction.OnTicketClick(item.ticket))
              is TicketListItem.Local -> onAction(TicketMasterAction.OnDraftClick(item.draft))
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
        )
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

private data class TicketItemData(
  val title: String,
  val subtitle: String,
  val date: String,
  val image: String?,
  val isDraft: Boolean,
  val isFavorite: Boolean,
  val isVoted: Boolean
)