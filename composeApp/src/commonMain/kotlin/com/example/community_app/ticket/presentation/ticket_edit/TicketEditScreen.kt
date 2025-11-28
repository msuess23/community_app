package com.example.community_app.ticket.presentation.ticket_edit

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.rememberAsyncImagePainter
import com.example.community_app.core.presentation.components.CommunityTopAppBar
import com.example.community_app.core.presentation.components.ObserveErrorMessage
import com.example.community_app.core.presentation.components.TopBarNavigationType
import com.example.community_app.core.presentation.components.button.CommunityButton
import com.example.community_app.core.presentation.components.dialog.CommunityDialog
import com.example.community_app.core.presentation.components.input.CommunityCheckbox
import com.example.community_app.core.presentation.components.input.CommunityDropdownMenu
import com.example.community_app.core.presentation.components.input.CommunityTextField
import com.example.community_app.core.presentation.helpers.toUiText
import com.example.community_app.core.presentation.theme.Spacing
import com.example.community_app.core.util.ImagePickerFactory
import com.example.community_app.util.TicketCategory
import com.example.community_app.util.TicketVisibility
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.auth_logout_dialog
import community_app.composeapp.generated.resources.auth_logout_label
import community_app.composeapp.generated.resources.cancel
import community_app.composeapp.generated.resources.category_singular
import community_app.composeapp.generated.resources.next
import community_app.composeapp.generated.resources.save
import community_app.composeapp.generated.resources.ticket_singular
import community_app.composeapp.generated.resources.info_singular
import community_app.composeapp.generated.resources.settings_radius_label
import compose.icons.FeatherIcons
import compose.icons.feathericons.Camera
import compose.icons.feathericons.Image
import compose.icons.feathericons.Plus
import compose.icons.feathericons.Trash2
import compose.icons.feathericons.X
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TicketEditScreenRoot(
  viewModel: TicketEditViewModel = koinViewModel(),
  onNavigateBack: () -> Unit
) {
  val state by viewModel.state.collectAsStateWithLifecycle()

  LaunchedEffect(state.isUploadSuccess, state.isDeleteSuccess) {
    if (state.isUploadSuccess || state.isDeleteSuccess) {
      onNavigateBack()
    }
  }

  TicketEditScreen(
    state = state,
    onAction = { action ->
      when (action) {
        TicketEditAction.OnNavigateBack -> onNavigateBack()
        else -> viewModel.onAction(action)
      }
    },
    onImagePicked = { viewModel.onImagePicked(it) }
  )
}

@Composable
private fun TicketEditScreen(
  state: TicketEditState,
  onAction: (TicketEditAction) -> Unit,
  onImagePicked: (ByteArray) -> Unit
) {
  val snackbarHostState = remember { SnackbarHostState() }

  // Image Picker Integration
  val imagePicker = ImagePickerFactory().createPicker()
  imagePicker.registerPicker(onImagePicked = onImagePicked)

  ObserveErrorMessage(
    errorMessage = state.errorMessage,
    snackbarHostState = snackbarHostState
  )

  Scaffold(
    topBar = {
      CommunityTopAppBar(
        titleContent = { Text(if (state.isDraft) "Entwurf bearbeiten" else "Ticket bearbeiten") },
        navigationType = TopBarNavigationType.Back,
        onNavigationClick = { onAction(TicketEditAction.OnNavigateBack) },
        actions = {
          if (state.isDraft || state.ticketId != null) {
            IconButton(onClick = { onAction(TicketEditAction.OnDeleteClick) }) {
              Icon(
                imageVector = FeatherIcons.Trash2,
                contentDescription = "Löschen",
                tint = MaterialTheme.colorScheme.error
              )
            }
          }
        }
      )
    },
    snackbarHost = { SnackbarHost(snackbarHostState) }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .verticalScroll(rememberScrollState())
        .padding(Spacing.screenPadding),
      verticalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {
      // Title & Description
      CommunityTextField(
        value = state.title,
        onValueChange = { onAction(TicketEditAction.OnTitleChange(it)) },
        label = Res.string.ticket_singular,
      )

      CommunityTextField(
        value = state.description,
        onValueChange = { onAction(TicketEditAction.OnDescriptionChange(it)) },
        label = Res.string.info_singular,
        singleLine = false,
        modifier = Modifier.height(120.dp)
      )

      // Category Picker
      CommunityDropdownMenu(
        items = TicketCategory.entries,
        selectedItem = state.category,
        onItemSelected = { onAction(TicketEditAction.OnCategoryChange(it)) },
        itemLabel = { it.toUiText().asString() },
        label = stringResource(Res.string.category_singular)
      )

      // Office Mock
      OutlinedButton(
        onClick = { /* TODO: Search Dialog */ },
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(text = "Zuständige Behörde: ${state.officeId ?: "Automatisch ermitteln"}")
      }

      // Location
      CommunityCheckbox(
        label = Res.string.settings_radius_label, // Fallback: "Standort verwenden" wäre besser
        checked = state.useCurrentLocation,
        onCheckChange = { onAction(TicketEditAction.OnUseLocationChange(it)) }
      )

      // Visibility
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text("Sichtbarkeit:", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.width(8.dp))
        CommunityDropdownMenu(
          items = TicketVisibility.entries,
          selectedItem = state.visibility,
          onItemSelected = { onAction(TicketEditAction.OnVisibilityChange(it)) },
          itemLabel = { it.name },
          modifier = Modifier.weight(1f)
        )
      }

      // Images Section
      Text("Bilder", style = MaterialTheme.typography.titleMedium)
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth().height(100.dp)
      ) {
        // Add Button
        item {
          Box(
            modifier = Modifier
              .size(100.dp)
              .clip(RoundedCornerShape(8.dp))
              .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
              .clickable { onAction(TicketEditAction.OnAddImageClick) },
            contentAlignment = Alignment.Center
          ) {
            Icon(FeatherIcons.Plus, null)
          }
        }

        // Image List
        items(state.images) { image ->
          val isCover = image.uri == state.coverImageUri
          Box(
            modifier = Modifier
              .size(100.dp)
              .clip(RoundedCornerShape(8.dp))
              .border(
                width = if (isCover) 3.dp else 0.dp,
                color = if (isCover) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent,
                shape = RoundedCornerShape(8.dp)
              )
              .clickable { onAction(TicketEditAction.OnSetCoverImage(image)) }
          ) {
            // Coil lädt auch lokale Dateien via Pfad/URI
            Image(
              painter = rememberAsyncImagePainter(image.uri),
              contentDescription = null,
              contentScale = ContentScale.Crop,
              modifier = Modifier.fillMaxSize()
            )

            // Delete Button Overlay
            Box(
              modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(24.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                .clickable { onAction(TicketEditAction.OnRemoveImage(image)) },
              contentAlignment = Alignment.Center
            ) {
              Icon(FeatherIcons.X, null, modifier = Modifier.size(16.dp))
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(Spacing.large))

      // Action Buttons
      if (state.isDraft) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          CommunityButton(
            text = Res.string.save,
            onClick = { onAction(TicketEditAction.OnSaveDraftClick) },
            modifier = Modifier.weight(1f)
          )
          CommunityButton(
            text = Res.string.next, // Use "Upload" Res ideally
            onClick = { onAction(TicketEditAction.OnUploadClick) },
            modifier = Modifier.weight(1f),
            enabled = state.title.isNotBlank() // Simple validation
          )
        }
      } else {
        CommunityButton(
          text = Res.string.save,
          onClick = { onAction(TicketEditAction.OnSaveTicketClick) },
          isLoading = state.isSaving
        )
      }
    }
  }

  // Dialogs
  if (state.showDeleteDialog) {
    CommunityDialog(
      title = Res.string.auth_logout_label, // Use "Löschen" Res
      text = Res.string.auth_logout_dialog, // Use "Wirklich löschen?" Res
      onDismissRequest = { onAction(TicketEditAction.OnDeleteDismiss) },
      confirmButtonText = Res.string.auth_logout_label,
      onConfirm = { onAction(TicketEditAction.OnDeleteConfirm) },
      dismissButtonText = Res.string.cancel,
      onDismiss = { onAction(TicketEditAction.OnDeleteDismiss) }
    )
  }

  if (state.showUploadDialog) {
    CommunityDialog(
      title = Res.string.next, // "Veröffentlichen"
      text = Res.string.next, // "Soll das Anliegen jetzt veröffentlicht werden?"
      onDismissRequest = { onAction(TicketEditAction.OnUploadDismiss) },
      confirmButtonText = Res.string.next,
      onConfirm = { onAction(TicketEditAction.OnUploadConfirm) }
    )
  }

  // Image Source Dialog
  if (state.showImageSourceDialog) {
    AlertDialog(
      onDismissRequest = { onAction(TicketEditAction.OnImageSourceDialogDismiss) },
      title = { Text("Bild hinzufügen") },
      text = {
        Column {
          ListItem(
            headlineContent = { Text("Kamera") },
            leadingContent = { Icon(FeatherIcons.Camera, null) },
            modifier = Modifier.clickable {
              onAction(TicketEditAction.OnImageSourceDialogDismiss)
              imagePicker.takePhoto()
            }
          )
          ListItem(
            headlineContent = { Text("Galerie") },
            leadingContent = { Icon(FeatherIcons.Image, null) },
            modifier = Modifier.clickable {
              onAction(TicketEditAction.OnImageSourceDialogDismiss)
              imagePicker.pickImage()
            }
          )
        }
      },
      confirmButton = {}
    )
  }
}