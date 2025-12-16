package com.example.community_app.ticket.presentation.ticket_detail

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.community_app.core.presentation.components.ObserveErrorMessage
import com.example.community_app.core.presentation.components.detail.DetailScreenLayout
import com.example.community_app.core.presentation.components.detail.InfoTicketDetailContent
import com.example.community_app.core.presentation.components.detail.StatusHistoryUiItem
import com.example.community_app.core.presentation.helpers.toUiText
import com.example.community_app.util.TicketStatus
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.draft_label
import community_app.composeapp.generated.resources.edit
import community_app.composeapp.generated.resources.ticket_singular
import compose.icons.FeatherIcons
import compose.icons.feathericons.Edit
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TicketDetailScreenRoot(
  viewModel: TicketDetailViewModel = koinViewModel(),
  onNavigateBack: () -> Unit,
  onNavigateToEdit: (Int?, Long?) -> Unit
) {
  val state by viewModel.state.collectAsStateWithLifecycle()

  TicketDetailScreen(
    state = state,
    onAction = { action ->
      when (action) {
        TicketDetailAction.OnNavigateBack -> onNavigateBack()
        TicketDetailAction.OnEditClick -> {
          val draft = state.draft
          val ticket = state.ticket

          if (state.isDraft && draft != null) {
            onNavigateToEdit(null, draft.id)
          } else if (!state.isDraft && ticket != null) {
            onNavigateToEdit(ticket.id, null)
          }
        }
        else -> viewModel.onAction(action)
      }
    }
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TicketDetailScreen(
  state: TicketDetailState,
  onAction: (TicketDetailAction) -> Unit
) {
  val category = if (state.isDraft) {
    state.draft?.category?.toUiText()?.asString()
  } else {
    state.ticket?.category?.toUiText()?.asString()
  }
  val titleRes = if (state.isDraft) Res.string.draft_label else Res.string.ticket_singular

  val displayTitle = state.ticket?.title ?: state.draft?.title
  val displayCategory = state.ticket?.category?.toUiText()?.asString()
    ?: state.draft?.category?.toUiText()?.asString() ?: "-"
  val displayDesc = state.ticket?.description ?: state.draft?.description
  val displayStatus = state.ticket?.currentStatus?.toUiText()?.asString()

  val historyUiItems = state.statusHistory.map { dto ->
    StatusHistoryUiItem(
      statusText = TicketStatus.valueOf(dto.status.toString()).toUiText().asString(),
      message = dto.message,
      createdAt = dto.createdAt
    )
  }

  val snackbarHostState = remember { SnackbarHostState() }

  ObserveErrorMessage(
    errorMessage = state.errorMessage,
    snackbarHostState = snackbarHostState,
    isLoading = (state.isLoading && (state.ticket == null && state.draft == null))
  )

  DetailScreenLayout(
    title = category ?: stringResource(titleRes),
    onNavigateBack = { onAction(TicketDetailAction.OnNavigateBack) },
    isLoading = state.isLoading,
    dataAvailable = displayTitle != null,
    showStatusHistory = state.showStatusHistory,
    statusHistory = historyUiItems,
    onDismissStatusHistory = { onAction(TicketDetailAction.OnDismissStatusHistory) },
    actions = {
      if (state.isOwner) {
        IconButton(onClick = { onAction(TicketDetailAction.OnEditClick) }) {
          Icon(
            imageVector = FeatherIcons.Edit,
            contentDescription = stringResource(Res.string.edit),
            tint = MaterialTheme.colorScheme.onPrimary
          )
        }
      }
    },
    snackbarHostState = snackbarHostState
  ) {
    InfoTicketDetailContent(
      title = displayTitle ?: "",
      category = displayCategory,
      description = displayDesc,
      images = state.imageUrls,
      statusText = displayStatus,
      startDate = null,
      endDate = null,
      onStatusClick = { onAction(TicketDetailAction.OnShowStatusHistory) },
      address = state.ticket?.address,
      isDraft = state.isDraft,
      isOwner = state.isOwner,
      isFavorite = state.ticket?.isFavorite ?: false,
      isVoted = state.ticket?.userVoted ?: false,
      votesCount = state.ticket?.votesCount ?: 0,
      onToggleFavorite = { onAction(TicketDetailAction.OnToggleFavorite) },
      onVote = { onAction(TicketDetailAction.OnVote) }
    )
  }
}