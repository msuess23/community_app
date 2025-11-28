package com.example.community_app.ticket.presentation.ticket_edit

import com.example.community_app.core.presentation.helpers.UiText
import com.example.community_app.util.TicketCategory
import com.example.community_app.util.TicketVisibility

data class TicketEditState(
  val isLoading: Boolean = false,
  val isSaving: Boolean = false,
  val errorMessage: UiText? = null,

  // Modus
  val isDraft: Boolean = true,
  val ticketId: Int? = null,
  val draftId: Long? = null,

  // Form Data
  val title: String = "",
  val description: String = "",
  val category: TicketCategory = TicketCategory.OTHER,
  val visibility: TicketVisibility = TicketVisibility.PUBLIC,
  val officeId: Int? = null,
  val useCurrentLocation: Boolean = true,

  // Images
  val images: List<TicketImageState> = emptyList(),
  val coverImageUri: String? = null,

  // Dialogs
  val showDeleteDialog: Boolean = false,
  val showUploadDialog: Boolean = false,
  val showImageSourceDialog: Boolean = false,

  // Permissions
  val cameraPermissionGranted: Boolean = false,
  val storagePermissionGranted: Boolean = false,
  val locationPermissionGranted: Boolean = false,

  val isUploadSuccess: Boolean = false,
  val isDeleteSuccess: Boolean = false
)

data class TicketImageState(
  val uri: String,
  val isLocal: Boolean,
  val id: String
)