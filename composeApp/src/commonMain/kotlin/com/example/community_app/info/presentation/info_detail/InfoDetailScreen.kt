package com.example.community_app.info.presentation.info_detail

import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.community_app.core.presentation.components.ObserveErrorMessage
import com.example.community_app.core.presentation.components.detail.DetailScreenLayout
import com.example.community_app.core.presentation.components.detail.InfoTicketDetailContent
import com.example.community_app.core.presentation.components.detail.StatusHistoryUiItem
import com.example.community_app.core.presentation.helpers.toUiText
import com.example.community_app.util.InfoStatus
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.info_singular
import compose.icons.FeatherIcons
import compose.icons.feathericons.Star
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun InfoDetailScreenRoot(
  viewModel: InfoDetailViewModel = koinViewModel(),
  onNavigateBack: () -> Unit
) {
  val state by viewModel.state.collectAsStateWithLifecycle()

  InfoDetailScreen(
    state = state,
    onAction = { action ->
      when (action) {
        InfoDetailAction.OnNavigateBack -> onNavigateBack()
        else -> viewModel.onAction(action)
      }
    }
  )

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InfoDetailScreen(
  state: InfoDetailState,
  onAction: (InfoDetailAction) -> Unit
) {
  val historyUiItems = state.statusHistory.map { dto ->
    StatusHistoryUiItem(
      statusText = InfoStatus.valueOf(dto.status.toString()).toUiText().asString(),
      message = dto.message,
      createdAt = dto.createdAt
    )
  }

  val snackbarHostState = remember { SnackbarHostState() }

  ObserveErrorMessage(
    errorMessage = state.errorMessage,
    snackbarHostState = snackbarHostState,
    isLoading = (state.isLoading && state.info == null)
  )

  DetailScreenLayout(
    title = state.info?.category?.toUiText()?.asString() ?: stringResource(Res.string.info_singular),
    onNavigateBack = { onAction(InfoDetailAction.OnNavigateBack) },
    isLoading = state.isLoading,
    dataAvailable = state.info != null,
    showStatusHistory = state.showStatusHistory,
    statusHistory = historyUiItems,
    onDismissStatusHistory = { onAction(InfoDetailAction.OnDismissStatusHistory) },
    actions = {
      FilledIconToggleButton(
        checked = state.info?.isFavorite ?: false,
        onCheckedChange = { onAction(InfoDetailAction.OnToggleFavorite) },
        colors = IconButtonDefaults.filledIconToggleButtonColors(
          contentColor = Color(0xFFFFD700),
          checkedContentColor = MaterialTheme.colorScheme.primary,
          containerColor = MaterialTheme.colorScheme.primary,
          checkedContainerColor = Color(0xFFFFD700)
        )
      ) {
        Icon(
          imageVector = FeatherIcons.Star,
          contentDescription = null,
          modifier = Modifier.size(28.dp)
        )
      }
    },
    snackbarHostState = snackbarHostState
  ) {
    state.info?.let { info ->
      InfoTicketDetailContent(
        title = info.title,
        category = info.category.toUiText().asString(),
        description = info.description,
        images = state.imageUrls,
        statusText = info.currentStatus?.toUiText()?.asString(),
        startDate = info.startsAt,
        endDate = info.endsAt,
        onStatusClick = { onAction(InfoDetailAction.OnShowStatusHistory) },
        address = info.address,
        isFavorite = info.isFavorite,
        isInfo = true,
        isDescExpanded = state.isDescriptionExpanded,
        onToggleFavorite = { onAction(InfoDetailAction.OnToggleFavorite) },
        onToggleDescription = { onAction(InfoDetailAction.OnToggleDescription) }
      )
    }
  }
}