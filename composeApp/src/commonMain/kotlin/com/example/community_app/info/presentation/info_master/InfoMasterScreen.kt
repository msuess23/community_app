package com.example.community_app.info.presentation.info_master

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.community_app.core.presentation.components.list.CustomList
import com.example.community_app.core.presentation.components.search.SearchBar
import com.example.community_app.info.domain.Info
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.no_search_results
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel


@Composable
fun InfoMasterScreenRoot(
  viewModel: InfoMasterViewModel = koinViewModel(),
  onInfoClick: (Info) -> Unit
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
    }
  )
}

@Composable
private fun InfoMasterScreen(
  state: InfoMasterState,
  onAction: (InfoMasterAction) -> Unit
) {
  val keyboardController = LocalSoftwareKeyboardController.current
  val lazyListState = rememberLazyListState()

  LaunchedEffect(state.searchResults) {
    lazyListState.animateScrollToItem(0)
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.primary)
      .statusBarsPadding(),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    SearchBar(
      searchQuery = state.searchQuery,
      onSearchQueryChange = {
        onAction(InfoMasterAction.OnSearchQueryChange(it))
      },
      onImeSearch = {
        keyboardController?.hide()
      },
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    )

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
        Box(
          modifier = Modifier
            .fillMaxSize(),
          contentAlignment = Alignment.Center
        ) {
          if(state.isLoading) {
            CircularProgressIndicator(
              color = MaterialTheme.colorScheme.primary
            )
          } else {
            when {
              state.errorMessage != null -> {
                Text(
                  text = state.errorMessage.asString(),
                  textAlign = TextAlign.Center,
                  style = MaterialTheme.typography.headlineSmall,
                  color = MaterialTheme.colorScheme.error
                )
              }
              state.searchResults.isEmpty() -> {
                Text(
                  text = stringResource(Res.string.no_search_results),
                  textAlign = TextAlign.Center,
                  style = MaterialTheme.typography.headlineSmall,
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
  }
}