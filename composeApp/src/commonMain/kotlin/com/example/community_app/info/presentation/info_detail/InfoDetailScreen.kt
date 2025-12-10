package com.example.community_app.info.presentation.info_detail

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.community_app.core.presentation.components.detail.DetailScreenLayout
import com.example.community_app.core.presentation.components.detail.InfoTicketDetailContent
import com.example.community_app.core.presentation.components.detail.StatusHistoryUiItem
import com.example.community_app.core.presentation.helpers.toUiText
import com.example.community_app.util.InfoStatus
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.info_singular
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

  DetailScreenLayout(
    title = state.info?.category?.toUiText()?.asString() ?: stringResource(Res.string.info_singular),
    onNavigateBack = { onAction(InfoDetailAction.OnNavigateBack) },
    isLoading = state.isLoading,
    dataAvailable = state.info != null,
    showStatusHistory = state.showStatusHistory,
    statusHistory = historyUiItems,
    onDismissStatusHistory = { onAction(InfoDetailAction.OnDismissStatusHistory) }
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
        address = info.address
      )
    }
  }
}