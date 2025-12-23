package com.example.community_app.appointment.presentation.detail.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.community_app.appointment.domain.model.AppointmentNote
import com.example.community_app.core.presentation.theme.Spacing
import com.example.community_app.core.util.formatIsoDate
import com.example.community_app.core.util.formatIsoTime
import com.example.community_app.core.util.toIso8601
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.at
import compose.icons.FeatherIcons
import compose.icons.feathericons.ChevronDown
import compose.icons.feathericons.ChevronUp
import compose.icons.feathericons.Delete
import org.jetbrains.compose.resources.stringResource

@Composable
fun NoteItem(
  note: AppointmentNote,
  onEdit: () -> Unit,
  onDelete: () -> Unit
) {
  var isExpanded by remember { mutableStateOf(false) }
  var isExpandable by remember { mutableStateOf(false) }

  val cardBackgroundColor = MaterialTheme.colorScheme.surfaceContainerLow

  Card(
    colors = CardDefaults.cardColors(
      containerColor = cardBackgroundColor,
    ),
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onEdit)
  ) {
    Row(
      modifier = Modifier
        .padding(Spacing.medium)
        .fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.Top
    ) {
      Column(
        modifier = Modifier
          .weight(1f)
          .animateContentSize()
      ) {
        Box(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = note.text,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = if (isExpanded) Int.MAX_VALUE else 2,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { textLayoutResult ->
              if (textLayoutResult.lineCount > 2 || textLayoutResult.hasVisualOverflow) {
                isExpandable = true
              }
            },
            modifier = Modifier.fillMaxWidth()
          )

          if (isExpandable) {
            Box(
              modifier = Modifier
                .align(Alignment.BottomEnd)
                .background(
                  Brush.horizontalGradient(
                    0.0f to Color.Transparent,
                    0.2f to cardBackgroundColor,
                    1.0f to cardBackgroundColor
                  )
                )
                .padding(start = 16.dp)
            ) {
              IconButton(
                onClick = { isExpanded = !isExpanded },
                modifier = Modifier.size(24.dp)
              ) {
                Icon(
                  imageVector = if (isExpanded) FeatherIcons.ChevronUp else FeatherIcons.ChevronDown,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(16.dp)
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.End
        ) {
          val date = formatIsoDate(toIso8601(note.createdAt))
          val time = formatIsoTime(toIso8601(note.createdAt))
          val at = stringResource(Res.string.at)

          Text(
            text = "$date $at $time",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      IconButton(
        onClick = onDelete,
        modifier = Modifier.size(24.dp)
      ) {
        Icon(
          imageVector = FeatherIcons.Delete,
          contentDescription = "Delete",
          tint = MaterialTheme.colorScheme.error,
          modifier = Modifier.size(20.dp)
        )
      }
    }
  }
}