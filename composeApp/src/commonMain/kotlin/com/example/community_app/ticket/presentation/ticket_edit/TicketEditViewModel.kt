package com.example.community_app.ticket.presentation.ticket_edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.community_app.app.navigation.Route
import com.example.community_app.core.data.local.FileStorage
import com.example.community_app.core.domain.Result
import com.example.community_app.core.domain.model.Address
import com.example.community_app.core.domain.usecase.FetchUserLocationUseCase
import com.example.community_app.core.presentation.helpers.toUiText
import com.example.community_app.core.util.getCurrentTimeMillis
import com.example.community_app.core.util.getFileNameFromPath
import com.example.community_app.media.domain.MediaRepository
import com.example.community_app.office.domain.OfficeRepository
import com.example.community_app.ticket.domain.EditableImage
import com.example.community_app.ticket.domain.TicketDraft
import com.example.community_app.ticket.domain.TicketRepository
import com.example.community_app.ticket.domain.usecase.edit.AddLocalImageUseCase
import com.example.community_app.ticket.domain.usecase.edit.DeleteTicketDataUseCase
import com.example.community_app.ticket.domain.usecase.edit.GetTicketEditDetailsUseCase
import com.example.community_app.ticket.domain.usecase.edit.UpdateTicketUseCase
import com.example.community_app.util.MediaTargetType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TicketEditViewModel(
  savedStateHandle: SavedStateHandle,
  private val ticketRepository: TicketRepository,
  private val mediaRepository: MediaRepository,
  private val officeRepository: OfficeRepository,
  private val fileStorage: FileStorage,
  private val fetchUserLocationUseCase: FetchUserLocationUseCase,
  private val getTicketEditDetailsUseCase: GetTicketEditDetailsUseCase,
  private val updateTicketUseCase: UpdateTicketUseCase,
  private val deleteTicketDataUseCase: DeleteTicketDataUseCase,
  private val addLocalImageUseCase: AddLocalImageUseCase
) : ViewModel() {

  private val args = savedStateHandle.toRoute<Route.TicketEdit>()

  private val _state = MutableStateFlow(TicketEditState())
  val state = _state
    .onStart { loadInitialData() }
    .stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000L),
      TicketEditState()
    )

  fun onAction(action: TicketEditAction) {
    when(action) {
      is TicketEditAction.OnTitleChange -> _state.update { it.copy(title = action.title) }
      is TicketEditAction.OnDescriptionChange -> _state.update { it.copy(description = action.description) }
      is TicketEditAction.OnCategoryChange -> _state.update { it.copy(category = action.category) }
      is TicketEditAction.OnVisibilityChange -> _state.update { it.copy(visibility = action.visibility) }
      is TicketEditAction.OnUseLocationChange -> handleLocationToggle(action.use)

      is TicketEditAction.OnOfficeQueryChange -> {
        _state.update { it.copy(officeSearchQuery = action.query) }
      }
      is TicketEditAction.OnOfficeSearchActiveChange -> {
        _state.update { it.copy(
          isOfficeSearchActive = action.active,
          officeSearchQuery = if (!action.active && it.selectedOffice != null) {
            it.selectedOffice.name
          } else it.officeSearchQuery
        ) }
      }
      is TicketEditAction.OnSelectOffice -> {
        _state.update { it.copy(
          officeId = action.office.id,
          selectedOffice = action.office,
          officeSearchQuery = action.office.name,
          isOfficeSearchActive = false
        ) }
      }

      TicketEditAction.OnAddImageClick -> _state.update { it.copy(showImageSourceDialog = true) }
      TicketEditAction.OnImageSourceDialogDismiss -> _state.update { it.copy(showImageSourceDialog = false) }

      is TicketEditAction.OnRemoveImage -> removeImage(action.image)
      is TicketEditAction.OnImageClick -> _state.update { it.copy(coverImageUri = action.image.uri) }

      TicketEditAction.OnSaveDraftClick -> saveDraft()

      TicketEditAction.OnUploadClick -> _state.update { it.copy(showUploadDialog = true) }
      TicketEditAction.OnUploadDismiss -> _state.update { it.copy(showUploadDialog = false) }
      TicketEditAction.OnUploadConfirm -> uploadDraft()

      TicketEditAction.OnSaveTicketClick -> saveTicket()

      TicketEditAction.OnDeleteClick -> _state.update { it.copy(showDeleteDialog = true) }
      TicketEditAction.OnDeleteDismiss -> _state.update { it.copy(showDeleteDialog = false) }
      TicketEditAction.OnDeleteConfirm -> deleteEntity()

      else -> Unit
    }
  }

  fun onImagePicked(tempPath: String) {
    viewModelScope.launch {
      val newImage = addLocalImageUseCase(tempPath)
      val uiImage = TicketImageState(newImage.uri, newImage.isLocal, newImage.id)

      _state.update { currentState ->
        val newImages = currentState.images + uiImage
        currentState.copy(
          images = newImages,
          coverImageUri = currentState.coverImageUri ?: uiImage.uri,
          showImageSourceDialog = false
        )
      }
    }
  }

  private fun loadInitialData() {
    viewModelScope.launch {
      _state.update { it.copy(isLoading = true) }

      launch {
        officeRepository.getOffices().collect { offices ->
          _state.update { currentState ->
            val updatedState = currentState.copy(availableOffices = offices)

            if (currentState.officeId != null && currentState.selectedOffice == null) {
              val match = offices.find { it.id == currentState.officeId }
              if (match != null) {
                updatedState.copy(selectedOffice = match, officeSearchQuery = match.name)
              } else updatedState
            } else updatedState
          }
        }
      }
      launch {
        officeRepository.refreshOffices()
      }

      val result = getTicketEditDetailsUseCase(args.ticketId, args.draftId)

      if (result is Result.Success) {
        val data = result.data

        if (data.officeId != null) {
          launch {
            officeRepository.refreshOffice(data.officeId)
          }
        }

        _state.update { it ->
          it.copy(
          isLoading = false,
          isDraft = data.isDraft,
          ticketId = data.ticketId,
          draftId = data.draftId,
          title = data.title,
          description = data.description,
          category = data.category,
          visibility = data.visibility,
          officeId = data.officeId,
          coverImageUri = data.coverImageUri,
          images = data.images.map {
            TicketImageState(it.uri, it.isLocal, it.id)
          }
        ) }
      } else {
        _state.update { it.copy(isLoading = false, isDraft = true) }
      }
    }
  }

  private fun handleLocationToggle(use: Boolean) {
    _state.update { it.copy(useCurrentLocation = use) }
    if (use) {
      viewModelScope.launch {
        val result = fetchUserLocationUseCase()
        _state.update { it.copy(locationPermissionGranted = result.permissionGranted) }
      }
    }
  }

  private fun removeImage(image: TicketImageState) {
    viewModelScope.launch {
      if (image.isLocal) {
        val fileName = getFileNameFromPath(image.uri)
        fileStorage.deleteImage(fileName)
      } else if (!state.value.isDraft && state.value.ticketId != null) {
        mediaRepository.deleteMedia(
          targetType = MediaTargetType.TICKET,
          targetId = state.value.ticketId!!,
          mediaId = image.id.toInt()
        )
      }
      _state.update { currentState ->
        val newImages = currentState.images - image
        val newCover = if (currentState.coverImageUri == image.uri) {
          newImages.firstOrNull()?.uri
        } else {
          currentState.coverImageUri
        }
        currentState.copy(images = newImages, coverImageUri = newCover)
      }
    }
  }

  private fun saveTicket() {
    viewModelScope.launch {
      val currentState = _state.value
      val ticketId = currentState.ticketId ?: return@launch
      _state.update { it.copy(isSaving = true, errorMessage = null) }

      val result = updateTicketUseCase(
        UpdateTicketUseCase.Params(
          ticketId = ticketId,
          title = currentState.title,
          description = currentState.description,
          category = currentState.category,
          visibility = currentState.visibility,
          officeId = currentState.officeId,
          coverImageUri = currentState.coverImageUri,
          images = currentState.images.map { EditableImage(it.uri, it.isLocal, it.id) }
        )
      )

      if (result is Result.Success) {
        _state.update { it.copy(isSaving = false, isUploadSuccess = true) }
      } else {
        val error = (result as Result.Error).error
        _state.update { it.copy(
          isSaving = false,
          errorMessage = error.toUiText()
        ) }
      }
    }
  }

  private fun saveDraft() {
    viewModelScope.launch {
      val currentState = _state.value
      val imageFileNames = currentState.images
        .filter { it.isLocal }
        .map { getFileNameFromPath(it.uri) }

      val draft = TicketDraft(
        id = currentState.draftId ?: 0L,
        title = currentState.title,
        description = currentState.description,
        category = currentState.category,
        officeId = currentState.officeId,
        visibility = currentState.visibility,
        images = imageFileNames,
        address = getAddressOrNull(),
        lastModified = getCurrentTimeMillis().toString()
      )
      ticketRepository.saveDraft(draft)
      _state.update { it.copy(isUploadSuccess = true) }
    }
  }

  private fun uploadDraft() {
    viewModelScope.launch {
      _state.update { it.copy(isSaving = true, showUploadDialog = false) }
      val currentState = _state.value
      val imageFileNames = currentState.images.filter { it.isLocal }.map { getFileNameFromPath(it.uri) }

      val finalOfficeId = currentState.officeId ?: 1

      val draft = TicketDraft(
        id = currentState.draftId ?: 0L,
        title = currentState.title,
        description = currentState.description,
        category = currentState.category,
        officeId = finalOfficeId,
        visibility = currentState.visibility,
        images = imageFileNames,
        address = getAddressOrNull(),
        lastModified = ""
      )

      val result = ticketRepository.uploadDraft(draft)

      if (result is Result.Success) {
        imageFileNames.forEach { fileStorage.deleteImage(it) }
        _state.update { it.copy(isSaving = false, isUploadSuccess = true) }
      } else {
        _state.update { it.copy(
          isSaving = false,
          errorMessage = (result as Result.Error).error.toUiText())
        }
      }
    }
  }

  private fun deleteEntity() {
    viewModelScope.launch {
      _state.update { it.copy(showDeleteDialog = false) }

      val result = deleteTicketDataUseCase(
        ticketId = _state.value.ticketId,
        draftId = _state.value.draftId,
        images = _state.value.images.map { EditableImage(it.uri, it.isLocal, it.id) }
      )

      if (result is Result.Success) {
        _state.update { it.copy(isDeleteSuccess = true) }
      } else {
        _state.update { it.copy(
          isDeleteSuccess = false,
          errorMessage = (result as Result.Error).error.toUiText())
        }
      }
    }
  }

  private suspend fun getAddressOrNull(): Address? {
    return if (_state.value.useCurrentLocation) {
      val result = fetchUserLocationUseCase()
      result.location?.let {
        Address(latitude = it.latitude, longitude = it.longitude)
      }
    } else null
  }
}