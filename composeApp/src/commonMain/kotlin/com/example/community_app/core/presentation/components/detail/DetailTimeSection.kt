package com.example.community_app.core.presentation.components.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import com.example.community_app.core.util.formatIsoDate
import com.example.community_app.core.util.formatIsoTime
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.at
import community_app.composeapp.generated.resources.draft_last_edited
import community_app.composeapp.generated.resources.from
import community_app.composeapp.generated.resources.on
import community_app.composeapp.generated.resources.point_in_time
import community_app.composeapp.generated.resources.ticket_uploaded_at
import community_app.composeapp.generated.resources.until
import compose.icons.FeatherIcons
import compose.icons.feathericons.Clock
import org.jetbrains.compose.resources.stringResource

@Composable
fun DetailTimeSection(
  startDate: String,
  endDate: String?,
  isInfo: Boolean,
  isDraft: Boolean
) {
  // Icons & Style
  val icon = FeatherIcons.Clock
  val iconTint = MaterialTheme.colorScheme.primary

  val startPrefix = if (endDate == null) {
    stringResource(Res.string.on)
  } else {
    stringResource(Res.string.from)
  }

  val at = stringResource(Res.string.at)
  val until = stringResource(Res.string.until)

  val startD = formatIsoDate(startDate)
  val startT = formatIsoTime(startDate)

  Card(
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    ),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier.padding(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = iconTint,
        modifier = Modifier
          .size(28.dp)
          .padding(top = 2.dp)
      )
      Spacer(modifier = Modifier.width(16.dp))
      if (isInfo) {
        Column(
          verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
          Text(
            text = stringResource(Res.string.point_in_time),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Text(
            text = "$startPrefix $startD $at $startT",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
          )

          if (endDate != null) {
            val endD = formatIsoDate(endDate)
            val endT = formatIsoTime(endDate)

            Text(
              text = "$until $endD $at $endT",
              style = MaterialTheme.typography.bodyLarge,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }
      }
      else if (isDraft) {
        Column {
          Text(
            text = stringResource(Res.string.draft_last_edited),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "$startPrefix $startD $at $startT",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
          )
        }
      }
      else {
        Text(
          text = stringResource(Res.string.ticket_uploaded_at) + " $startD",
          style = MaterialTheme.typography.bodyLarge,
          color = MaterialTheme.colorScheme.onSurface
        )
      }
    }
  }
}