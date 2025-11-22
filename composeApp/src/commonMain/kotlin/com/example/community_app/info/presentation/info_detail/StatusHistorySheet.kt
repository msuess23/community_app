package com.example.community_app.info.presentation.info_detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.community_app.core.presentation.helpers.toUiText
import com.example.community_app.dto.StatusDto
import com.example.community_app.util.InfoStatus
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.label_status_history
import compose.icons.FeatherIcons
import compose.icons.feathericons.Activity
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusHistorySheet(
  history: List<StatusDto>,
  onAction: (InfoDetailAction) -> Unit,
  modifier: Modifier = Modifier
) {
  val sheetState = rememberModalBottomSheetState()

  ModalBottomSheet(
    onDismissRequest = {
      onAction(InfoDetailAction.OnDismissStatusHistory)
    },
    sheetState = sheetState,
    contentColor = MaterialTheme.colorScheme.surfaceContainer,
    modifier = modifier
  ) {
    Column(
      modifier = Modifier
        .padding(horizontal = 24.dp)
        .padding(bottom = 48.dp)
    ) {
      Text(
        text = stringResource(Res.string.label_status_history),
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(bottom = 16.dp)
      )

      LazyColumn {
        items(history) { statusItem ->
          ListItem(
            headlineContent = {
              val statusEnum = try {
                InfoStatus.valueOf(statusItem.status.name)
              } catch (e: Exception) {
                InfoStatus.SCHEDULED
              }
              Text(statusEnum.toUiText().asString())
            },
            supportingContent = {
              statusItem.message?.let { Text(it) }
            },
            leadingContent = {
              Icon(
                imageVector = FeatherIcons.Activity,
                contentDescription = null
              )
            },
            trailingContent = {
              Text(
                text = statusItem.createdAt.take(10),
                style = MaterialTheme.typography.labelSmall
              )
            }
          )
          HorizontalDivider()
        }
      }
    }
  }
}