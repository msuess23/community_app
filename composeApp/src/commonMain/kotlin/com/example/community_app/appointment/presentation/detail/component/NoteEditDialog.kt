package com.example.community_app.appointment.presentation.detail.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.community_app.core.presentation.components.dialog.CommunityDialog
import com.example.community_app.core.presentation.components.input.CommunityTextField
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.appointment_plural
import community_app.composeapp.generated.resources.appointment_singular
import community_app.composeapp.generated.resources.cancel
import community_app.composeapp.generated.resources.next
import community_app.composeapp.generated.resources.note_create
import community_app.composeapp.generated.resources.note_edit
import community_app.composeapp.generated.resources.note_singular
import community_app.composeapp.generated.resources.save

@Composable
fun NoteEditDialog(
  initialText: String,
  isEdit: Boolean,
  onDismiss: () -> Unit,
  onConfirm: (String) -> Unit
) {
  var text by remember(initialText) { mutableStateOf(initialText) }

  CommunityDialog(
    title = if (isEdit) Res.string.note_edit else Res.string.note_create,
    onDismissRequest = onDismiss,
    confirmButtonText = Res.string.save,
    onConfirm = { onConfirm(text) },
    dismissButtonText = Res.string.cancel,
    onDismiss = onDismiss
  ) {
    Column(
      modifier = Modifier.fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      CommunityTextField(
        value = text,
        onValueChange = { text = it },
        label = Res.string.note_singular,
        modifier = Modifier.fillMaxWidth(),
        singleLine = false
      )
    }
  }
}