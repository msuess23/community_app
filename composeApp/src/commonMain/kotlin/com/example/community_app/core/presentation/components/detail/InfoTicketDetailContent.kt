package com.example.community_app.core.presentation.components.detail

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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.community_app.media.presentation.ImageGallery
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.label_status
import community_app.composeapp.generated.resources.label_status_history
import compose.icons.FeatherIcons
import compose.icons.feathericons.Activity
import org.jetbrains.compose.resources.stringResource

@Composable
fun InfoTicketDetailContent(
  title: String,
  category: String,
  description: String?,
  images: List<String>,
  statusText: String?,
  onStatusClick: () -> Unit,
  startDate: String?,
  endDate: String?,
  addressContent: @Composable (() -> Unit)? = null
) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
  ) {
    ImageGallery(
      imageUrls = images,
      modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(16f / 9f)
    )

    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Column {
        Text(
          text = title,
          style = MaterialTheme.typography.headlineMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
      }

      statusText?.let { status ->
        Card(
          onClick = onStatusClick,
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
                text = status,
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

      description?.let { desc ->
        Text(
          text = desc,
          style = MaterialTheme.typography.bodyLarge,
          color = MaterialTheme.colorScheme.onSurface
        )
      }

      addressContent?.let {
        Card(
          modifier = Modifier.fillMaxWidth().height(150.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
          Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            it()
          }
        }
      }
    }
  }
}