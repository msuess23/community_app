package com.example.community_app.core.presentation.components.detail

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.community_app.core.util.formatIsoDate
import com.example.community_app.core.util.formatIsoTime
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.label_status_history
import compose.icons.FeatherIcons
import compose.icons.feathericons.Activity
import org.jetbrains.compose.resources.stringResource

data class StatusHistoryUiItem(
  val statusText: String,
  val message: String?,
  val createdAt: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusHistorySheet(
  history: List<StatusHistoryUiItem>,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  val sheetState = rememberModalBottomSheetState()

  ModalBottomSheet(
    onDismissRequest = onDismiss,
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
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 16.dp)
      )

      LazyColumn {
        items(history) { item ->
          ListItem(
            headlineContent = {
              Text(item.statusText)
            },
            supportingContent = {
              item.message?.let { Text(it) }
            },
            leadingContent = {
              Icon(
                imageVector = FeatherIcons.Activity,
                contentDescription = null
              )
            },
            trailingContent = {
              Text(
                text = "${formatIsoDate(item.createdAt)}, ${formatIsoTime(item.createdAt)}",
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