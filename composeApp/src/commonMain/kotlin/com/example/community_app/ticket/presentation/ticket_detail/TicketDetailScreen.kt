package com.example.community_app.ticket.presentation.ticket_detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.community_app.core.presentation.components.CommunityTopAppBar
import com.example.community_app.core.presentation.components.TopBarNavigationType
import com.example.community_app.core.presentation.components.detail.InfoTicketDetailContent
import com.example.community_app.core.presentation.components.detail.StatusHistorySheet
import com.example.community_app.core.presentation.components.detail.StatusHistoryUiItem
import com.example.community_app.core.presentation.helpers.toUiText
import com.example.community_app.util.TicketStatus
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.info_not_found
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
  Scaffold(
    topBar = {
      CommunityTopAppBar(
        titleContent = {
          val title = if (state.isDraft) "Entwurf" else stringResource(Res.string.ticket_singular)
          Text(title)
        },
        navigationType = TopBarNavigationType.Back,
        onNavigationClick = { onAction(TicketDetailAction.OnNavigateBack) },
        actions = {
          if (state.isOwner) {
            IconButton(onClick = { onAction(TicketDetailAction.OnEditClick) }) {
              Icon(
                imageVector = FeatherIcons.Edit,
                contentDescription = "Bearbeiten", // TODO: Res
                tint = MaterialTheme.colorScheme.onPrimary
              )
            }
          }
        }
      )
    }
  ) { padding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .background(MaterialTheme.colorScheme.surface)
    ) {
      if (state.isLoading) {
        CircularProgressIndicator(
          modifier = Modifier.align(Alignment.Center),
          color = MaterialTheme.colorScheme.primary
        )
      } else {
        val title = state.ticket?.title ?: state.draft?.title

        if (title != null) {
          val category = state.ticket?.category?.toUiText()?.asString()
            ?: state.draft?.category?.toUiText()?.asString()
            ?: "-"

          val description = state.ticket?.description ?: state.draft?.description

          val statusText = state.ticket?.currentStatus?.toUiText()?.asString()

          val images = state.imageUrls.ifEmpty { listOfNotNull(state.ticket?.imageUrl) }

          InfoTicketDetailContent(
            title = title,
            category = category,
            description = description,
            images = images,
            statusText = statusText,
            startDate = null,
            endDate = null,
            onStatusClick = { onAction(TicketDetailAction.OnShowStatusHistory) },
            addressContent = { Text("Map Placeholder") }, // TODO: Real map
            isDraft = state.isDraft,
            isOwner = state.isOwner
          )
        } else {
          Text(
            text = stringResource(Res.string.info_not_found),
            modifier = Modifier.align(Alignment.Center)
          )
        }
      }
    }
  }

  if (state.showStatusHistory) {
    val history = state.statusHistory.map { dto ->
      val statusText = TicketStatus.valueOf(dto.status.toString()).toUiText().asString()

      StatusHistoryUiItem(
        statusText = statusText,
        message = dto.message,
        createdAt = dto.createdAt
      )
    }

    StatusHistorySheet(
      history = history,
      onDismiss = { onAction(TicketDetailAction.OnDismissStatusHistory) }
    )
  }
}