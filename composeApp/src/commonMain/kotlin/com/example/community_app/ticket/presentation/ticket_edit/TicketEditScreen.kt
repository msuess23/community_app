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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
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
import community_app.composeapp.generated.resources.auth_logout_label
import community_app.composeapp.generated.resources.camera
import community_app.composeapp.generated.resources.cancel
import community_app.composeapp.generated.resources.category_singular
import community_app.composeapp.generated.resources.delete
import community_app.composeapp.generated.resources.draft_edit_label
import community_app.composeapp.generated.resources.gallery
import community_app.composeapp.generated.resources.images_label
import community_app.composeapp.generated.resources.next
import community_app.composeapp.generated.resources.save
import community_app.composeapp.generated.resources.ticket_singular
import community_app.composeapp.generated.resources.info_singular
import community_app.composeapp.generated.resources.ticket_delete_dialog_text
import community_app.composeapp.generated.resources.ticket_edit_label
import community_app.composeapp.generated.resources.ticket_upload_dialog_text
import community_app.composeapp.generated.resources.ticket_visibility_info_text
import community_app.composeapp.generated.resources.ticket_visibility_label
import community_app.composeapp.generated.resources.upload
import community_app.composeapp.generated.resources.use_current_location
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
        titleContent = { Text(text =
          if (state.isDraft) stringResource(Res.string.draft_edit_label)
          else stringResource(Res.string.ticket_edit_label)
        ) },
        navigationType = TopBarNavigationType.Back,
        onNavigationClick = { onAction(TicketEditAction.OnNavigateBack) },
        actions = {
          if (state.isDraft || state.ticketId != null) {
            IconButton(onClick = { onAction(TicketEditAction.OnDeleteClick) }) {
              Icon(
                imageVector = FeatherIcons.Trash2,
                contentDescription = stringResource(Res.string.delete),
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
        keyboardOptions = KeyboardOptions(
          keyboardType = KeyboardType.Text,
          imeAction = ImeAction.Next
        )
      )

      CommunityTextField(
        value = state.description,
        onValueChange = { onAction(TicketEditAction.OnDescriptionChange(it)) },
        label = Res.string.info_singular,
        singleLine = false,
        keyboardOptions = KeyboardOptions(
          keyboardType = KeyboardType.Text,
          imeAction = ImeAction.Done
        ),
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

      // Visibility
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
      ) {
        CommunityDropdownMenu(
          items = TicketVisibility.entries,
          selectedItem = state.visibility,
          onItemSelected = { onAction(TicketEditAction.OnVisibilityChange(it)) },
          itemLabel = { it.toUiText().asString() },
          label = stringResource(Res.string.ticket_visibility_label)
        )
        Text(
          text = stringResource(Res.string.ticket_visibility_info_text),
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(Spacing.extraSmall)
        )
      }

      // TODO: SearchBar with result
      // (https://developer.android.com/develop/ui/compose/components/search-bar?hl=en)
      OutlinedButton(
        onClick = { /* TODO: Search Dialog */ },
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(text = "Zuständige Behörde: ${state.officeId ?: "Automatisch ermitteln"}")
      }

      // Location
      CommunityCheckbox(
        label = Res.string.use_current_location,
        checked = state.useCurrentLocation,
        onCheckChange = { onAction(TicketEditAction.OnUseLocationChange(it)) }
      )

      // Images Section
      Text(
        text = stringResource(Res.string.images_label),
        style = MaterialTheme.typography.titleMedium
      )
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
                color = if (isCover) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
              )
              .clickable { onAction(TicketEditAction.OnSetCoverImage(image)) }
          ) {
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
            modifier = Modifier.weight(1f),
            enabled = state.title.isNotBlank()
          )
          CommunityButton(
            text = Res.string.upload,
            onClick = { onAction(TicketEditAction.OnUploadClick) },
            modifier = Modifier.weight(1f),
            enabled = state.title.isNotBlank()
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
      title = Res.string.delete,
      text = if (state.isDraft) Res.string.ticket_delete_dialog_text else Res.string.ticket_delete_dialog_text,
      onDismissRequest = { onAction(TicketEditAction.OnDeleteDismiss) },
      confirmButtonText = Res.string.auth_logout_label,
      onConfirm = { onAction(TicketEditAction.OnDeleteConfirm) },
      dismissButtonText = Res.string.cancel,
      onDismiss = { onAction(TicketEditAction.OnDeleteDismiss) }
    )
  }

  if (state.showUploadDialog) {
    CommunityDialog(
      title = Res.string.upload,
      text = Res.string.ticket_upload_dialog_text,
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
            headlineContent = { Text(stringResource(Res.string.camera)) },
            leadingContent = { Icon(FeatherIcons.Camera, null) },
            modifier = Modifier.clickable {
              onAction(TicketEditAction.OnImageSourceDialogDismiss)
              imagePicker.takePhoto()
            }
          )
          ListItem(
            headlineContent = { Text(stringResource(Res.string.gallery)) },
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