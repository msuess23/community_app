package com.example.community_app.info.presentation.info_detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.community_app.core.presentation.helpers.toUiText
import com.example.community_app.info.domain.Info
import com.example.community_app.media.presentation.ImageGallery
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.button_back
import community_app.composeapp.generated.resources.info_not_found
import community_app.composeapp.generated.resources.info_singular
import community_app.composeapp.generated.resources.label_status
import community_app.composeapp.generated.resources.label_status_history
import compose.icons.FeatherIcons
import compose.icons.feathericons.Activity
import compose.icons.feathericons.ChevronLeft
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
      TopAppBar(
        title = {
          state.info?.let { info ->
            Text(info.category.toUiText().asString())
          } ?: run {
            stringResource(Res.string.info_singular)
          }
        },
        navigationIcon = {
          IconButton(onClick = {
            onAction(InfoDetailAction.OnNavigateBack)
          }) {
            Icon(
              imageVector = FeatherIcons.ChevronLeft,
              contentDescription = stringResource(Res.string.button_back)
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.primary,
          titleContentColor = MaterialTheme.colorScheme.onPrimary,
          navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
          actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
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
          Column(
            modifier = Modifier
              .fillMaxSize()
              .verticalScroll(rememberScrollState())
          ) {
            ImageGallery(
              imageUrls = state.imageUrls.ifEmpty {
                listOfNotNull(info.imageUrl)
              },
              modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
            )
            InfoDetailContent(info, onAction)
          }

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
    StatusHistorySheet(
      history = state.statusHistory,
      onAction = onAction
    )
  }
}

@Composable
private fun InfoDetailContent(
  info: Info,
  onAction: (InfoDetailAction) -> Unit
) {
  Column(
    modifier = Modifier.padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Column {
      Text(
        text = info.title,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )
    }

    info.currentStatus?.let { status ->
      Card(
        onClick = { onAction(InfoDetailAction.OnShowStatusHistory) },
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column {
            Text(
              text = stringResource(Res.string.label_status),
              style = MaterialTheme.typography.labelMedium
            )
            Text(
              text = status.toUiText().asString(),
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
          }
          Icon(
            imageVector = FeatherIcons.Activity,
            contentDescription = stringResource(Res.string.label_status_history)
          )
        }
      }
    }

    info.description?.let { desc ->
      Text(
        text = desc,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface
      )
    }

    info.address?.let {
      Card(
        modifier = Modifier.fillMaxWidth().height(150.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
      ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Text("MapCard Placeholder")
        }
      }
    }
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