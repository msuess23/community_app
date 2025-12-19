package com.example.community_app.ticket.presentation.ticket_edit

import com.example.community_app.core.presentation.helpers.UiText
import com.example.community_app.office.domain.Office
import com.example.community_app.util.TicketCategory
import com.example.community_app.util.TicketVisibility

data class TicketEditState(
  // Status Flags & Error
  val isLoading: Boolean = false,
  val isSaving: Boolean = false,
  val isUploadSuccess: Boolean = false,
  val isDeleteSuccess: Boolean = false,
  val errorMessage: UiText? = null,

  // Mode
  val isDraft: Boolean = true,
  val ticketId: Int? = null,
  val draftId: Long? = null,

  // Form Data
  val title: String = "",
  val description: String = "",
  val category: TicketCategory = TicketCategory.OTHER,
  val visibility: TicketVisibility = TicketVisibility.PRIVATE,
  val officeId: Int? = null,
  val useCurrentLocation: Boolean = true,

  // Office Selection
  val availableOffices: List<Office> = emptyList(),
  val officeSearchQuery: String = "",
  val isOfficeSearchActive: Boolean = false,
  val selectedOffice: Office? = null,

  // Images
  val images: List<TicketImageState> = emptyList(),
  val coverImageUri: String? = null,

  // Dialogs
  val showDeleteDialog: Boolean = false,
  val showUploadDialog: Boolean = false,
  val showImageSourceDialog: Boolean = false
) {
  val filteredOffices: List<Office>
    get() = if (officeSearchQuery.isBlank()) {
      availableOffices
    } else {
      availableOffices.filter {
        it.name.contains(officeSearchQuery, ignoreCase = true)
            || (it.services?.contains(officeSearchQuery, ignoreCase = true) == true)
      }
    }
}

data class TicketImageState(
  val uri: String,
  val isLocal: Boolean,
  val id: String
)