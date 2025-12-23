package com.example.community_app.appointment.presentation.detail.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.community_app.appointment.domain.model.AppointmentNote
import com.example.community_app.core.presentation.components.list.ScreenMessage
import com.example.community_app.core.presentation.theme.Spacing
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.note_none
import community_app.composeapp.generated.resources.note_plural
import compose.icons.FeatherIcons
import compose.icons.feathericons.Plus
import org.jetbrains.compose.resources.stringResource

@Composable
fun NotesSection(
  notes: List<AppointmentNote>,
  onAddClick: () -> Unit,
  onEditClick: (AppointmentNote) -> Unit,
  onDeleteClick: (AppointmentNote) -> Unit
) {
  Column(
    modifier = Modifier.fillMaxWidth()
  ) {
    HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.medium))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = stringResource(Res.string.note_plural),
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
      )
      IconButton(onClick = onAddClick) {
        Icon(
          imageVector = FeatherIcons.Plus,
          contentDescription = "Add Note",
          tint = MaterialTheme.colorScheme.primary
        )
      }
    }

    Spacer(modifier = Modifier.height(Spacing.small))

    if (notes.isEmpty()) {
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        ScreenMessage(
          text = stringResource(Res.string.note_none),
          color = MaterialTheme.colorScheme.onSurface
        )
      }
    } else {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        notes.forEach { note ->
          NoteItem(
            note = note,
            onEdit = { onEditClick(note) },
            onDelete = { onDeleteClick(note) }
          )
        }
      }
    }
  }
}