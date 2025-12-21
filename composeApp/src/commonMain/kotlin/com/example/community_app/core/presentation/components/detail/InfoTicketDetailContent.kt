package com.example.community_app.core.presentation.components.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.community_app.geocoding.domain.Address
import com.example.community_app.media.presentation.ImageGallery
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.draft_label_long
import community_app.composeapp.generated.resources.label_status
import community_app.composeapp.generated.resources.label_status_history
import community_app.composeapp.generated.resources.ticket_mine_label
import community_app.composeapp.generated.resources.ticket_votes_label
import compose.icons.FeatherIcons
import compose.icons.feathericons.Activity
import compose.icons.feathericons.Star
import compose.icons.feathericons.ThumbsUp
import compose.icons.feathericons.User
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
  address: Address?,
  isDraft: Boolean = false,
  isOwner: Boolean = false,
  isFavorite: Boolean = false,
  isInfo: Boolean = false,
  isVoted: Boolean = false,
  votesCount: Int = 0,
  onToggleFavorite: () -> Unit = {},
  onVote: () -> Unit = {}
) {
  val scrollState = rememberScrollState()

  Column(
    modifier = Modifier
      .fillMaxSize()
      .verticalScroll(scrollState)
  ) {
    if (images.isNotEmpty()) {
      ImageGallery(
        imageUrls = images,
        modifier = Modifier
          .fillMaxWidth()
          .aspectRatio(16f / 9f)
      )
    }

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

        if (isDraft) {
          SuggestionChip(
            onClick = {},
            label = { Text(stringResource(Res.string.draft_label_long)) },
            colors = SuggestionChipDefaults.suggestionChipColors(
              containerColor = MaterialTheme.colorScheme.tertiaryContainer,
              labelColor = MaterialTheme.colorScheme.onTertiaryContainer
            ),
            modifier = Modifier.padding(bottom = 8.dp)
          )
        } else if (isOwner) {
          SuggestionChip(
            onClick = {},
            label = { Text(stringResource(Res.string.ticket_mine_label)) },
            icon = { Icon(FeatherIcons.User, null) },
            modifier = Modifier.padding(bottom = 8.dp)
          )
        }
      }

      if (startDate != null) {
        DetailTimeSection(
          startDate = startDate,
          endDate = endDate,
          isInfo = isInfo,
          isDraft = isDraft
        )
      }

      description?.let { desc ->
        Text(
          text = desc,
          style = MaterialTheme.typography.bodyLarge,
          color = MaterialTheme.colorScheme.onSurface
        )
      }

      if (!isDraft && !isInfo) {
        Card(
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
          ),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .padding(8.dp)
              .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              IconToggleButton(
                checked = isVoted,
                onCheckedChange = { onVote() }
              ) {
                Icon(
                  imageVector = FeatherIcons.ThumbsUp,
                  contentDescription = null,
                  tint = if (isVoted) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(28.dp)
                )
              }

              Text(
                text = "$votesCount " + stringResource(Res.string.ticket_votes_label),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
            }

            if (!isOwner) {
              IconToggleButton(
                checked = isFavorite,
                onCheckedChange = { onToggleFavorite() }
              ) {
                Icon(
                  imageVector = FeatherIcons.Star,
                  contentDescription = null,
                  tint = if (isFavorite) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(28.dp)
                )
              }
            }
          }
        }
      }

      if (!isDraft && statusText != null) {
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
                text = statusText,
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

      if (address != null) {
        CommunityAddressCard(address = address)
      }
    }
  }
}


