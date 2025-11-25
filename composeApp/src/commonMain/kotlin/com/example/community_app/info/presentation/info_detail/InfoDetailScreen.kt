package com.example.community_app.info.presentation.info_detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.community_app.core.presentation.components.CommunityTopAppBar
import com.example.community_app.core.presentation.components.TopBarNavigationType
import com.example.community_app.core.presentation.components.detail.InfoTicketDetailContent
import com.example.community_app.core.presentation.components.detail.StatusHistorySheet
import com.example.community_app.core.presentation.components.detail.StatusHistoryUiItem
import com.example.community_app.core.presentation.helpers.toUiText
import com.example.community_app.util.InfoStatus
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.info_not_found
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
  Scaffold(
    topBar = {
      CommunityTopAppBar(
        titleContent = {
          val titleText = state.info?.category?.toUiText()?.asString()
            ?: stringResource(Res.string.info_singular)
          Text(titleText)
        },
        navigationType = TopBarNavigationType.Back,
        onNavigationClick = { onAction(InfoDetailAction.OnNavigateBack) },
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
        state.info?.let { info ->
          InfoTicketDetailContent(
            title = info.title,
            category = info.category.toUiText().asString(),
            description = info.description,
            images = state.imageUrls.ifEmpty {
              listOfNotNull(info.imageUrl)
            },
            statusText = info.currentStatus?.toUiText()?.asString(),
            startDate = info.startsAt,
            endDate = info.endsAt,
            onStatusClick = { onAction(InfoDetailAction.OnShowStatusHistory) },
            addressContent = { if (info.address != null) Text("MapCard Placeholder") }
          )
        } ?: run {
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
      val statusText = InfoStatus.valueOf(dto.status.toString()).toUiText().asString()

      StatusHistoryUiItem(
        statusText = statusText,
        message = dto.message,
        createdAt = dto.createdAt
      )
    }

    StatusHistorySheet(
      history = history,
      onDismiss = { onAction(InfoDetailAction.OnDismissStatusHistory) }
    )
  }
}


//
//      Spacer(modifier = Modifier.height(8.dp))
//
//      Surface(
//        color = MaterialTheme.colorScheme.secondaryContainer,
//        shape = RoundedCornerShape(8.dp)
//      ) {
//        Text(
//          text = info.category.toUiText().asString(),
//          style = MaterialTheme.typography.labelLarge,
//          color = MaterialTheme.colorScheme.onSecondaryContainer,
//          modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
//        )
//      }