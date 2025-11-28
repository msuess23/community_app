package com.example.community_app.ticket.presentation.ticket_edit

import com.example.community_app.util.TicketCategory
import com.example.community_app.util.TicketVisibility

sealed interface TicketEditAction {
  // Form Changes
  data class OnTitleChange(val title: String) : TicketEditAction
  data class OnDescriptionChange(val description: String) : TicketEditAction
  data class OnCategoryChange(val category: TicketCategory) : TicketEditAction
  data class OnVisibilityChange(val visibility: TicketVisibility) : TicketEditAction
  data class OnUseLocationChange(val use: Boolean) : TicketEditAction

  // Image Actions
  data object OnAddImageClick : TicketEditAction
  data class OnImageSourceSelected(val source: ImageSource) : TicketEditAction
  data object OnImageSourceDialogDismiss : TicketEditAction
  data class OnRemoveImage(val image: TicketImageState) : TicketEditAction
  data class OnSetCoverImage(val image: TicketImageState) : TicketEditAction
  data class OnImageClick(val image: TicketImageState) : TicketEditAction

  // Main Actions
  data object OnSaveDraftClick : TicketEditAction
  data object OnUploadClick : TicketEditAction
  data object OnSaveTicketClick : TicketEditAction
  data object OnDeleteClick : TicketEditAction

  // Dialog Actions
  data object OnDeleteConfirm : TicketEditAction
  data object OnDeleteDismiss : TicketEditAction
  data object OnUploadConfirm : TicketEditAction
  data object OnUploadDismiss : TicketEditAction

  data object OnNavigateBack : TicketEditAction
}

enum class ImageSource {
  CAMERA, GALLERY
}