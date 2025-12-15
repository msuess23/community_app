package com.example.community_app.office.presentation.office_master

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.community_app.auth.presentation.components.AuthGuard
import com.example.community_app.core.presentation.components.ObserveErrorMessage
import com.example.community_app.core.presentation.components.list.ScreenMessage
import com.example.community_app.core.presentation.components.master.MasterScreenLayout
import com.example.community_app.core.presentation.components.master.SingleTabHeader
import com.example.community_app.office.domain.Office
import com.example.community_app.office.presentation.office_master.component.OfficeFilterSheet
import com.example.community_app.office.presentation.office_master.component.OfficeListItem
import com.example.community_app.ticket.presentation.ticket_master.TicketMasterAction
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.office_plural
import community_app.composeapp.generated.resources.search_no_results
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OfficeMasterScreenRoot(
  viewModel: OfficeMasterViewModel = koinViewModel(),
  onOfficeClick: (Office) -> Unit,
  onOpenDrawer: () -> Unit
) {
  val state by viewModel.state.collectAsStateWithLifecycle()

  OfficeMasterScreen(
    state = state,
    onAction = { action ->
      when(action) {
        is OfficeMasterAction.OnOfficeClick -> onOfficeClick(action.office)
        else -> Unit
      }
      viewModel.onAction(action)
    },
    onOpenDrawer = onOpenDrawer
  )
}

@Composable
private fun OfficeMasterScreen(
  state: OfficeMasterState,
  onAction: (OfficeMasterAction) -> Unit,
  onOpenDrawer: () -> Unit
) {
  val lazyListState = rememberLazyListState()
  val snackbarHostState = remember { SnackbarHostState() }

  LaunchedEffect(state.searchResults) {
    lazyListState.animateScrollToItem(0)
  }

  ObserveErrorMessage(
    errorMessage = state.errorMessage,
    snackbarHostState = snackbarHostState,
    isLoading = (state.isLoading && state.searchResults.isEmpty())
  )

  MasterScreenLayout(
    searchQuery = state.searchQuery,
    isFilterActive = state.filter.distanceRadiusKm < 50f || state.filter.sortBy != OfficeSortOption.ALPHABETICAL,
    isLoading = state.isLoading,
    isEmpty = state.searchResults.isEmpty(),
    snackbarHostState = snackbarHostState,
    onSearchQueryChange = { onAction(OfficeMasterAction.OnSearchQueryChange(it)) },
    onRefresh = { onAction(OfficeMasterAction.OnRefresh) },
    onOpenDrawer = onOpenDrawer,
    onToggleFilterSheet = { onAction(OfficeMasterAction.OnToggleFilterSheet) },
    tabsContent = { SingleTabHeader(Res.string.office_plural) },
    emptyStateContent = {
      ScreenMessage(
        text = stringResource(Res.string.search_no_results),
        color = MaterialTheme.colorScheme.onSurface
      )
    }
  ) {
    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      state = lazyListState,
      verticalArrangement = Arrangement.spacedBy(12.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      items(
        items = state.searchResults,
        key = { it.id }
      ) { office ->
        OfficeListItem(
          office = office,
          onClick = { onAction(OfficeMasterAction.OnOfficeClick(office)) },
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
        )
      }
    }
  }

  if (state.isFilterSheetVisible) {
    OfficeFilterSheet(
      filterState = state.filter,
      onAction = onAction
    )
  }
}