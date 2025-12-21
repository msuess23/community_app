package com.example.community_app.ticket.presentation.ticket_edit

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.community_app.core.presentation.components.CommunityTopAppBar
import com.example.community_app.core.presentation.components.ObserveErrorMessage
import com.example.community_app.core.presentation.components.TopBarNavigationType
import com.example.community_app.core.presentation.components.button.CommunityButton
import com.example.community_app.core.presentation.components.detail.CommunityAddressCard
import com.example.community_app.core.presentation.components.dialog.CommunityDialog
import com.example.community_app.core.presentation.components.input.CommunityCheckbox
import com.example.community_app.core.presentation.components.input.CommunityDropdownMenu
import com.example.community_app.core.presentation.components.input.CommunityTextField
import com.example.community_app.core.presentation.helpers.toUiText
import com.example.community_app.core.presentation.theme.Spacing
import com.example.community_app.core.util.ImagePickerFactory
import com.example.community_app.geocoding.presentation.AddressSearchOverlay
import com.example.community_app.office.presentation.office_master.component.OfficeListItem
import com.example.community_app.profile.presentation.ProfileAction
import com.example.community_app.ticket.presentation.ticket_edit.component.ImageSelectionSection
import com.example.community_app.ticket.presentation.ticket_edit.component.OfficePlaceholderCard
import com.example.community_app.ticket.presentation.ticket_edit.component.OfficeSearchOverlay
import com.example.community_app.util.TicketCategory
import com.example.community_app.util.TicketVisibility
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.camera
import community_app.composeapp.generated.resources.cancel
import community_app.composeapp.generated.resources.category_singular
import community_app.composeapp.generated.resources.delete
import community_app.composeapp.generated.resources.draft_edit_label
import community_app.composeapp.generated.resources.gallery
import community_app.composeapp.generated.resources.save
import community_app.composeapp.generated.resources.ticket_singular
import community_app.composeapp.generated.resources.info_singular
import community_app.composeapp.generated.resources.ticket_add_image
import community_app.composeapp.generated.resources.ticket_create_label
import community_app.composeapp.generated.resources.ticket_delete_dialog_text
import community_app.composeapp.generated.resources.ticket_edit_label
import community_app.composeapp.generated.resources.ticket_empty_address_info
import community_app.composeapp.generated.resources.ticket_upload_dialog_text
import community_app.composeapp.generated.resources.ticket_visibility_info_text
import community_app.composeapp.generated.resources.ticket_visibility_label
import community_app.composeapp.generated.resources.upload
import community_app.composeapp.generated.resources.use_current_location
import compose.icons.FeatherIcons
import compose.icons.feathericons.Camera
import compose.icons.feathericons.Image
import compose.icons.feathericons.Trash2
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TicketEditScreenRoot(
  viewModel: TicketEditViewModel = koinViewModel(),
  onNavigateBack: () -> Unit,
  onNavigateToMaster: () -> Unit
) {
  val state by viewModel.state.collectAsStateWithLifecycle()

  LaunchedEffect(state.isUploadSuccess, state.isDeleteSuccess) {
    if (state.isUploadSuccess || state.isDeleteSuccess) {
      onNavigateToMaster()
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TicketEditScreen(
  state: TicketEditState,
  onAction: (TicketEditAction) -> Unit,
  onImagePicked: (String) -> Unit
) {
  val snackbarHostState = remember { SnackbarHostState() }
  var expanded by rememberSaveable { mutableStateOf(false) }

  // Image Picker Integration
  val imagePicker = ImagePickerFactory().createPicker()
  imagePicker.registerPicker(onImagePicked = onImagePicked)

  ObserveErrorMessage(
    errorMessage = state.errorMessage,
    snackbarHostState = snackbarHostState
  )

  Box(
    modifier = Modifier.fillMaxSize()
  ) {
    Scaffold(
      topBar = {
        CommunityTopAppBar(
          titleContent = {
            val titleRes = when {
              (!state.isDraft && state.ticketId != null) -> Res.string.ticket_edit_label
              (state.isDraft && state.draftId != null) -> Res.string.draft_edit_label
              else -> Res.string.ticket_create_label
            }
            Text(text = stringResource(titleRes))
          },
          navigationType = TopBarNavigationType.Back,
          onNavigationClick = { onAction(TicketEditAction.OnNavigateBack) },
          actions = {
            if (state.draftId != null || state.ticketId != null) {
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

        // Office
        if (state.selectedOffice != null) {
          OfficeListItem(
            office = state.selectedOffice,
            onClick = { onAction(TicketEditAction.OnOfficeSearchActiveChange(true)) }
          )
        } else {
          OfficePlaceholderCard(
            onClick = { onAction(TicketEditAction.OnOfficeSearchActiveChange(true)) }
          )
        }

        // Location
        CommunityAddressCard(
          address = state.selectedAddress,
          onClick = { onAction(TicketEditAction.OnAddressSearchActiveChange(true)) },
          label = stringResource(Res.string.ticket_empty_address_info),
        )

        // Images Section
        ImageSelectionSection(
          coverImageUri = state.coverImageUri,
          images = state.images,
          onAddImage = { onAction(TicketEditAction.OnAddImageClick) },
          onImageClick = { onAction(TicketEditAction.OnImageClick(it)) },
          onRemoveImage = { onAction(TicketEditAction.OnRemoveImage(it)) },
        )

        Spacer(modifier = Modifier.height(Spacing.large))

        // Action Buttons
        if (state.isDraft) {
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CommunityButton(
              text = Res.string.save,
              onClick = { onAction(TicketEditAction.OnSaveDraftClick) },
              modifier = Modifier.weight(1f),
              isLoading = state.isSaving,
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
            isLoading = state.isSaving,
            enabled = state.title.isNotBlank()
          )
        }
      }
    }
  }

  // Office Search Overlay
  if (state.isOfficeSearchActive) {
    OfficeSearchOverlay(
      query = state.officeSearchQuery,
      onQueryChange = { onAction(TicketEditAction.OnOfficeQueryChange(it)) },
      onSearch = {},
      offices = state.filteredOffices,
      onOfficeClick = { onAction(TicketEditAction.OnSelectOffice(it)) },
      onBackClick = { onAction(TicketEditAction.OnOfficeSearchActiveChange(false)) }
    )
  }

  // Address Search Overlay
  if (state.isAddressSearchActive) {
    AddressSearchOverlay(
      query = state.addressSearchQuery,
      onQueryChange = { onAction(TicketEditAction.OnAddressQueryChange(it)) },
      isLocationAvailable = state.currentLocation != null,
      suggestions = state.addressSuggestions,
      onAddressClick = { onAction(TicketEditAction.OnSelectAddress(it)) },
      onUseCurrentLocationClick = { onAction(TicketEditAction.OnUseCurrentLocationClick) },
      onBackClick = { onAction(TicketEditAction.OnAddressSearchActiveChange(false)) }
    )
  }

  // Dialogs
  if (state.showDeleteDialog) {
    CommunityDialog(
      title = Res.string.delete,
      text = if (state.isDraft) Res.string.ticket_delete_dialog_text else Res.string.ticket_delete_dialog_text,
      onDismissRequest = { onAction(TicketEditAction.OnDeleteDismiss) },
      confirmButtonText = Res.string.delete,
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
      confirmButtonText = Res.string.upload,
      onConfirm = { onAction(TicketEditAction.OnUploadConfirm) },
      dismissButtonText = Res.string.cancel,
      onDismiss = { onAction(TicketEditAction.OnUploadDismiss) }
    )
  }

  // Image Source Dialog
  if (state.showImageSourceDialog) {
    AlertDialog(
      onDismissRequest = { onAction(TicketEditAction.OnImageSourceDialogDismiss) },
      title = { Text(stringResource(Res.string.ticket_add_image)) },
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