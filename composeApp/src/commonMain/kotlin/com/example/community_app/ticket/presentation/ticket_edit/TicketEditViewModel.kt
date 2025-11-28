package com.example.community_app.ticket.presentation.ticket_edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.community_app.app.navigation.Route
import com.example.community_app.core.data.local.FileStorage
import com.example.community_app.core.domain.Result
import com.example.community_app.core.domain.location.LocationService
import com.example.community_app.core.domain.permission.AppPermissionService
import com.example.community_app.core.presentation.helpers.toUiText
import com.example.community_app.media.domain.MediaRepository
import com.example.community_app.ticket.domain.TicketDraft
import com.example.community_app.ticket.domain.TicketRepository
import com.example.community_app.util.BASE_URL
import com.example.community_app.util.MediaTargetType
import com.example.community_app.util.TicketCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TicketEditViewModel(
  savedStateHandle: SavedStateHandle,
  private val ticketRepository: TicketRepository,
  private val mediaRepository: MediaRepository,
  private val locationService: LocationService,
  private val permissionService: AppPermissionService,
  private val fileStorage: FileStorage // NEU: Injiziert
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

      TicketEditAction.OnAddImageClick -> _state.update { it.copy(showImageSourceDialog = true) }
      TicketEditAction.OnImageSourceDialogDismiss -> _state.update { it.copy(showImageSourceDialog = false) }

      is TicketEditAction.OnRemoveImage -> removeImage(action.image)
      is TicketEditAction.OnSetCoverImage -> _state.update { it.copy(coverImageUri = action.image.uri) }
      is TicketEditAction.OnImageClick -> { }

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

  // Callback vom ImagePicker: Speichert Bytes direkt persistent
  fun onImagePicked(bytes: ByteArray) {
    viewModelScope.launch {
      // 1. In Dateisystem schreiben
      val path = fileStorage.saveImage(bytes)

      // 2. Pfad in den State aufnehmen
      val newImage = TicketImageState(
        uri = path, // Das ist jetzt ein echter Dateipfad
        isLocal = true,
        id = path
      )

      _state.update { currentState ->
        val newImages = currentState.images + newImage
        currentState.copy(
          images = newImages,
          // Wenn erstes Bild -> automatisch Cover
          coverImageUri = currentState.coverImageUri ?: newImage.uri,
          showImageSourceDialog = false
        )
      }
    }
  }

  private fun loadInitialData() {
    if (args.ticketId != null) {
      loadTicket(args.ticketId)
    } else if (args.draftId != null) {
      loadDraft(args.draftId)
    } else {
      _state.update { it.copy(isDraft = true, draftId = null) }
    }
  }

  private fun loadTicket(id: Int) {
    viewModelScope.launch {
      _state.update { it.copy(isLoading = true, isDraft = false, ticketId = id) }
      val ticket = ticketRepository.getTicket(id).first()
      val mediaResult = mediaRepository.getMediaList(MediaTargetType.TICKET, id)

      if (ticket != null) {
        val images = if (mediaResult is Result.Success) {
          mediaResult.data.map {
            TicketImageState(
              uri = "$BASE_URL${it.url}",
              isLocal = false,
              id = it.id.toString()
            )
          }
        } else emptyList()

        _state.update { it.copy(
          isLoading = false,
          title = ticket.title,
          description = ticket.description ?: "",
          category = ticket.category,
          visibility = ticket.visibility,
          officeId = ticket.officeId,
          images = images,
          coverImageUri = ticket.imageUrl
        )}
      }
    }
  }

  private fun loadDraft(id: Long) {
    viewModelScope.launch {
      val draft = ticketRepository.getDraft(id)
      if (draft != null) {
        // Hier laden wir die Pfade aus der DB.
        // Da wir 'FileStorage' nutzen, sind diese Pfade persistent.
        val images = draft.images.map { path ->
          TicketImageState(uri = path, isLocal = true, id = path)
        }

        _state.update { it.copy(
          isDraft = true,
          draftId = id,
          title = draft.title,
          description = draft.description ?: "",
          category = draft.category ?: TicketCategory.OTHER,
          visibility = draft.visibility,
          officeId = draft.officeId,
          images = images,
          coverImageUri = images.firstOrNull()?.uri
        )}
      }
    }
  }

  private fun handleLocationToggle(use: Boolean) {
    _state.update { it.copy(useCurrentLocation = use) }
    if (use) {
      viewModelScope.launch {
        val granted = permissionService.requestLocationPermission()
        _state.update { it.copy(locationPermissionGranted = granted) }
      }
    }
  }

  private fun removeImage(image: TicketImageState) {
    viewModelScope.launch {
      // Wenn lokal, Datei physisch löschen
      if (image.isLocal) {
        fileStorage.deleteImage(image.uri)
      }
      // Wenn Remote, Server-Delete
      else if (!state.value.isDraft && state.value.ticketId != null) {
        mediaRepository.deleteMedia(MediaTargetType.TICKET, state.value.ticketId!!, image.id.toInt())
      }

      _state.update { currentState ->
        val newImages = currentState.images - image
        // Cover zurücksetzen falls gelöscht
        val newCover = if (currentState.coverImageUri == image.uri) newImages.firstOrNull()?.uri else currentState.coverImageUri
        currentState.copy(images = newImages, coverImageUri = newCover)
      }
    }
  }

  private fun saveDraft() {
    viewModelScope.launch {
      val currentState = _state.value

      val draft = TicketDraft(
        id = currentState.draftId ?: 0L,
        title = currentState.title,
        description = currentState.description,
        category = currentState.category,
        officeId = currentState.officeId,
        visibility = currentState.visibility,
        // Wir speichern nur die Pfade in der DB. Die Dateien liegen sicher im FileStorage.
        images = currentState.images.filter { it.isLocal }.map { it.uri },
        address = if (currentState.useCurrentLocation) {
          locationService.getCurrentLocation()?.let {
            com.example.community_app.core.domain.model.Address(
              latitude = it.latitude, longitude = it.longitude
            )
          }
        } else null,
        lastModified = com.example.community_app.core.util.getCurrentTimeMillis().toString()
      )

      ticketRepository.saveDraft(draft)
      _state.update { it.copy(isUploadSuccess = true) } // Navigate back
    }
  }

  private fun uploadDraft() {
    viewModelScope.launch {
      _state.update { it.copy(isSaving = true, showUploadDialog = false) }
      val currentState = _state.value

      val draft = TicketDraft(
        id = currentState.draftId ?: 0L,
        title = currentState.title,
        description = currentState.description,
        category = currentState.category,
        officeId = 1, // Mock
        visibility = currentState.visibility,
        images = currentState.images.filter { it.isLocal }.map { it.uri },
        address = if (currentState.useCurrentLocation) {
          locationService.getCurrentLocation()?.let {
            com.example.community_app.core.domain.model.Address(
              latitude = it.latitude, longitude = it.longitude
            )
          }
        } else null,
        lastModified = ""
      )

      // Image Provider: Liest Datei vom Speicher
      val imageProvider: suspend (String) -> ByteArray? = { path ->
        fileStorage.readImage(path)
      }

      val result = ticketRepository.uploadDraft(draft, imageProvider)

      if (result is Result.Success) {
        // Bei Erfolg: Lokale Dateien aufräumen
        draft.images.forEach { fileStorage.deleteImage(it) }
        _state.update { it.copy(isSaving = false, isUploadSuccess = true) }
      } else {
        _state.update { it.copy(isSaving = false, errorMessage = (result as Result.Error).error.toUiText()) }
      }
    }
  }

  private fun saveTicket() {
    viewModelScope.launch {
      val currentState = _state.value
      val ticketId = currentState.ticketId ?: return@launch

      _state.update { it.copy(isSaving = true) }

      // 1. Metadata Update
      ticketRepository.updateTicket(
        id = ticketId,
        title = currentState.title,
        description = currentState.description,
        category = currentState.category,
        officeId = currentState.officeId,
        address = null,
        visibility = currentState.visibility
      )

      // 2. Upload neuer lokaler Bilder (die noch nicht auf dem Server sind)
      currentState.images.filter { it.isLocal }.forEach { img ->
        fileStorage.readImage(img.uri)?.let { bytes ->
          mediaRepository.uploadMedia(MediaTargetType.TICKET, ticketId, bytes)
          // Nach Upload lokal löschen
          fileStorage.deleteImage(img.uri)
        }
      }

      // 3. Update Cover (falls ein Server-Bild gewählt wurde)
      currentState.images.find { it.uri == currentState.coverImageUri && !it.isLocal }?.let { cover ->
        mediaRepository.setCover(cover.id.toInt())
      }

      _state.update { it.copy(isSaving = false, isUploadSuccess = true) }
    }
  }

  private fun deleteEntity() {
    viewModelScope.launch {
      _state.update { it.copy(showDeleteDialog = false) }

      if (_state.value.isDraft) {
        // Draft: Dateien löschen
        state.value.images.filter { it.isLocal }.forEach { fileStorage.deleteImage(it.uri) }
        _state.value.draftId?.let { ticketRepository.deleteDraft(it) }
      } else {
        // Ticket: Server delete
        _state.value.ticketId?.let { ticketRepository.deleteTicket(it) }
      }

      _state.update { it.copy(isDeleteSuccess = true) }
    }
  }
}