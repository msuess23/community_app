package com.example.community_app.info.presentation.info_master

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
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
import com.example.community_app.core.presentation.components.ObserveErrorMessage
import com.example.community_app.core.presentation.components.list.InfoTicketListItem
import com.example.community_app.core.presentation.components.master.MasterScreenLayout
import com.example.community_app.core.presentation.components.master.SingleTabHeader
import com.example.community_app.core.presentation.helpers.toUiText
import com.example.community_app.core.util.formatIsoDate
import com.example.community_app.info.domain.Info
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.info_plural
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
    isFilterActive = state.filter.selectedCategories.isNotEmpty() || state.filter.selectedStatuses.isNotEmpty(),
    isLoading = state.isLoading,
    isEmpty = state.searchResults.isEmpty(),
    snackbarHostState = snackbarHostState,
    onSearchQueryChange = { onAction(InfoMasterAction.OnSearchQueryChange(it)) },
    onRefresh = { onAction(InfoMasterAction.OnRefresh) },
    onOpenDrawer = onOpenDrawer,
    onToggleFilterSheet = { onAction(InfoMasterAction.OnToggleFilterSheet) },
    tabsContent = { SingleTabHeader(Res.string.info_plural) }
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

  if (state.isFilterSheetVisible) {
    InfoFilterSheet(
      filterState = state.filter,
      onAction = onAction
    )
  }
}