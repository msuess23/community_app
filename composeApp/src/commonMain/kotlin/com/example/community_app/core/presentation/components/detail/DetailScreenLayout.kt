package com.example.community_app.core.presentation.components.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.community_app.core.presentation.components.CommunityTopAppBar
import com.example.community_app.core.presentation.components.TopBarNavigationType
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.error_data_not_found
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreenLayout(
  // Header
  title: String,
  onNavigateBack: () -> Unit,
  actions: @Composable RowScope.() -> Unit = {},

  // State
  isLoading: Boolean,
  dataAvailable: Boolean,

  // Status Sheet Config (optional)
  showStatusHistory: Boolean = false,
  statusHistory: List<StatusHistoryUiItem> = emptyList(),
  onDismissStatusHistory: () -> Unit = {},

  // Content Slot
  content: @Composable () -> Unit
) {
  Scaffold(
    topBar = {
      CommunityTopAppBar(
        titleContent = { Text(title) },
        navigationType = TopBarNavigationType.Back,
        onNavigationClick = onNavigateBack,
        actions = actions
      )
    }
  ) { padding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .background(MaterialTheme.colorScheme.surface)
    ) {
      if (isLoading) {
        CircularProgressIndicator(
          modifier = Modifier.align(Alignment.Center),
          color = MaterialTheme.colorScheme.primary
        )
      } else if (!dataAvailable) {
        Text(
          text = stringResource(Res.string.error_data_not_found),
          modifier = Modifier.align(Alignment.Center)
        )
      } else {
        content()
      }
    }
  }

  if (showStatusHistory) {
    StatusHistorySheet(
      history = statusHistory,
      onDismiss = onDismissStatusHistory
    )
  }
}