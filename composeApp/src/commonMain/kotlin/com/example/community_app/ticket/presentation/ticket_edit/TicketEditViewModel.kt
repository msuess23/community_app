package com.example.community_app.ticket.presentation.ticket_edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.community_app.app.navigation.Route
import com.example.community_app.core.domain.Result
import com.example.community_app.core.domain.usecase.FetchUserLocationUseCase
import com.example.community_app.core.presentation.helpers.toUiText
import com.example.community_app.geocoding.domain.Address
import com.example.community_app.geocoding.domain.usecase.AddToAddressHistoryUseCase
import com.example.community_app.geocoding.domain.usecase.GetAddressFromLocationUseCase
import com.example.community_app.geocoding.domain.usecase.GetAddressSuggestionsUseCase
import com.example.community_app.office.domain.Office
import com.example.community_app.ticket.domain.EditableImage
import com.example.community_app.ticket.domain.TicketEditInput
import com.example.community_app.ticket.domain.usecase.edit.AddLocalImageUseCase
import com.example.community_app.ticket.domain.usecase.edit.DeleteTicketDataUseCase
import com.example.community_app.ticket.domain.usecase.edit.DiscardLocalImagesUseCase
import com.example.community_app.ticket.domain.usecase.edit.GetTicketEditDetailsUseCase
import com.example.community_app.ticket.domain.usecase.edit.OperationResult
import com.example.community_app.ticket.domain.usecase.edit.SaveDraftUseCase
import com.example.community_app.ticket.domain.usecase.edit.UpdateTicketUseCase
import com.example.community_app.ticket.domain.usecase.edit.UploadDraftUseCase
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TicketEditViewModel(
  savedStateHandle: SavedStateHandle,
  private val observeTicketEditData: GetTicketEditDetailsUseCase,
  private val updateTicket: UpdateTicketUseCase,
  private val deleteTicketData: DeleteTicketDataUseCase,
  private val addLocalImage: AddLocalImageUseCase,
  private val saveDraft: SaveDraftUseCase,
  private val uploadDraft: UploadDraftUseCase,
  private val discardLocalImages: DiscardLocalImagesUseCase,
  private val getAddressSuggestions: GetAddressSuggestionsUseCase,
  private val fetchUserLocation: FetchUserLocationUseCase,
  private val getAddressFromLocation: GetAddressFromLocationUseCase,
  private val addToAddressHistory: AddToAddressHistoryUseCase
) : ViewModel() {
  private val args = savedStateHandle.toRoute<Route.TicketEdit>()
  private val _state = MutableStateFlow(TicketEditState())

  private var imagesPendingDeletion: Set<TicketImageState> = emptySet()
  private var newlyAddedLocalUris: Set<String> = emptySet()

  private val currentInput: TicketEditInput
    get() = with(_state.value) {
      TicketEditInput(
        title = title,
        description = description,
        category = category,
        visibility = visibility,
        officeId = officeId,
        address = selectedAddress,
        images = images.map { EditableImage(it.uri, it.isLocal, it.id) }
      )
    }

  val state = _state
    .onStart {
      loadData()
      observeAddressSearchQuery()
    }
    .stateIn(
      viewModelScope,
      SharingStarted.WhileSubscribed(5000L),
      TicketEditState()
    )

  fun onAction(action: TicketEditAction) {
    when (action) {
      // Form Input
      is TicketEditAction.OnTitleChange -> _state.update { it.copy(title = action.title) }
      is TicketEditAction.OnDescriptionChange -> _state.update { it.copy(description = action.description) }
      is TicketEditAction.OnCategoryChange -> _state.update { it.copy(category = action.category) }
      is TicketEditAction.OnVisibilityChange -> _state.update { it.copy(visibility = action.visibility) }

      // Office Search
      is TicketEditAction.OnOfficeQueryChange -> _state.update { it.copy(officeSearchQuery = action.query) }
      is TicketEditAction.OnOfficeSearchActiveChange -> toggleOfficeSearch(action.active)
      is TicketEditAction.OnSelectOffice -> selectOffice(action.office)

      // Address Selection
      is TicketEditAction.OnAddressSearchActiveChange -> {
        _state.update {
          it.copy(
            isAddressSearchActive = action.active,
            addressSearchQuery = if (!action.active) "" else it.addressSearchQuery
          )
        }
        if (action.active) fetchCurrentLocation()
      }
      is TicketEditAction.OnAddressQueryChange -> {
        _state.update { it.copy(addressSearchQuery = action.query) }
      }
      is TicketEditAction.OnSelectAddress -> selectAddress(action.address)
      TicketEditAction.OnUseCurrentLocationClick -> useCurrentLocation()

      // Dialogs & Images
      TicketEditAction.OnAddImageClick -> _state.update { it.copy(showImageSourceDialog = true) }
      TicketEditAction.OnImageSourceDialogDismiss -> _state.update { it.copy(showImageSourceDialog = false) }
      TicketEditAction.OnUploadClick -> _state.update { it.copy(showUploadDialog = true) }
      TicketEditAction.OnUploadDismiss -> _state.update { it.copy(showUploadDialog = false) }
      TicketEditAction.OnDeleteClick -> _state.update { it.copy(showDeleteDialog = true) }
      TicketEditAction.OnDeleteDismiss -> _state.update { it.copy(showDeleteDialog = false) }
      is TicketEditAction.OnRemoveImage -> markImageForRemoval(action.image)
      is TicketEditAction.OnImageClick -> _state.update { it.copy(coverImageUri = action.image.uri) }

      // Operations
      TicketEditAction.OnUploadConfirm -> uploadDraft()
      TicketEditAction.OnSaveTicketClick -> saveTicket()
      TicketEditAction.OnSaveDraftClick -> saveDraft()
      TicketEditAction.OnDeleteConfirm -> deleteEntity()

      else -> Unit
    }
  }

  private fun loadData() {
    viewModelScope.launch {
      _state.update { it.copy(isLoading = true) }

      observeTicketEditData(args.ticketId, args.draftId).collect { result ->
        when (result) {
          is Result.Success -> {
            val data = result.data
            _state.update { current ->
              val base = if (current.ticketId == null && current.draftId == null && data.details != null) {
                current.copy(
                  isDraft = data.details.isDraft,
                  ticketId = data.details.ticketId,
                  draftId = data.details.draftId,
                  title = data.details.title,
                  description = data.details.description,
                  category = data.details.category,
                  visibility = data.details.visibility,
                  officeId = data.details.officeId,
                  selectedAddress = data.details.address,
                  coverImageUri = data.details.coverImageUri,
                  images = data.details.images.map { TicketImageState(it.uri, it.isLocal, it.id) }
                )
              } else current

              val selected = data.offices.find { it.id == base.officeId }

              base.copy(
                isLoading = false,
                availableOffices = data.offices,
                selectedOffice = selected ?: base.selectedOffice,
                officeSearchQuery = base.officeSearchQuery.ifBlank { selected?.name ?: "" },
                errorMessage = data.syncError?.toUiText() ?: current.errorMessage
              )
            }
          }
          is Result.Error -> {
            _state.update { it.copy(
              isLoading = false,
              errorMessage = result.error.toUiText()
            ) }
          }
        }
      }
    }
  }

  @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
  private fun observeAddressSearchQuery() {
    _state.map { it.addressSearchQuery }
      .distinctUntilChanged()
      .debounce(500L)
      .flatMapLatest { query ->
        getAddressSuggestions(query)
      }
      .onEach { suggestions ->
        _state.update { it.copy(addressSuggestions = suggestions) }
      }
      .launchIn(viewModelScope)
  }

  private fun selectAddress(address: Address) {
    viewModelScope.launch {
      _state.update {
        it.copy(
          selectedAddress = address,
          isAddressSearchActive = false,
          addressSearchQuery = ""
        )
      }
      addToAddressHistory(address)
    }
  }

  private fun fetchCurrentLocation() {
    viewModelScope.launch {
      val result = fetchUserLocation()
      if (result.location != null) {
        _state.update { it.copy(currentLocation = result.location) }
      }
    }
  }

  private fun useCurrentLocation() {
    viewModelScope.launch {
      val location = _state.value.currentLocation ?: return@launch

      when (val result = getAddressFromLocation(location.latitude, location.longitude)) {
        is Result.Success -> {
          result.data?.let { address ->
            _state.update {
              it.copy(
                selectedAddress = address,
                isAddressSearchActive = false
              )
            }
            addToAddressHistory(address)
          }
        }
        is Result.Error -> {
          _state.update { it.copy(errorMessage = result.error.toUiText()) }
        }
      }
    }
  }

  private fun saveTicket() {
    val s = _state.value
    val ticketId = s.ticketId ?: return

    viewModelScope.launch {
      updateTicket(
        ticketId = ticketId,
        input = currentInput,
        imagesToDelete = imagesPendingDeletion.map {
          EditableImage(it.uri, it.isLocal, it.id)
        }.toSet(),
        coverImageUri = s.coverImageUri
      ).collect { applyOperationResult(it) }
    }
  }

  private fun uploadDraft() {
    viewModelScope.launch {
      _state.update { it.copy(showUploadDialog = false) }

      uploadDraft(
        draftId = _state.value.draftId,
        input = currentInput
      ).collect { applyOperationResult(it) }
    }
  }

  private fun saveDraft() {
    viewModelScope.launch {
      saveDraft(
        draftId = _state.value.draftId,
        input = currentInput
      ).collect { applyOperationResult(it) }
    }
  }

  private fun deleteEntity() {
    viewModelScope.launch {
      _state.update { it.copy(showDeleteDialog = false) }
      val result = deleteTicketData(
        ticketId = _state.value.ticketId,
        draftId = _state.value.draftId,
        images = _state.value.images.map { EditableImage(it.uri, it.isLocal, it.id) }
      )
      if (result is Result.Success) _state.update { it.copy(isDeleteSuccess = true) }
      else _state.update { it.copy(errorMessage = (result as Result.Error).error.toUiText()) }
    }
  }

  private fun applyOperationResult(result: OperationResult) {
    when(result) {
      OperationResult.Loading -> _state.update { it.copy(isSaving = true, errorMessage = null) }
      OperationResult.Success -> _state.update { it.copy(isSaving = false, isUploadSuccess = true) }
      is OperationResult.Error -> _state.update { it.copy(isSaving = false, errorMessage = result.message) }
    }
  }

  fun onImagePicked(tempPath: String) {
    viewModelScope.launch {
      val newImage = addLocalImage(tempPath)
      val uiImage = TicketImageState(newImage.uri, newImage.isLocal, newImage.id)
      _state.update {
        it.copy(
          images = it.images + uiImage,
          coverImageUri = it.coverImageUri ?: uiImage.uri,
          showImageSourceDialog = false
        )
      }
    }
  }

  private fun markImageForRemoval(image: TicketImageState) {
    val isFresh = image.uri in newlyAddedLocalUris

    if (image.isLocal && isFresh) {
      viewModelScope.launch { discardLocalImages(setOf(image.uri)) }
      newlyAddedLocalUris = newlyAddedLocalUris - image.uri
      _state.update { it.copy(images = it.images - image) }
    } else {
      imagesPendingDeletion = imagesPendingDeletion + image

      _state.update {
        val newImages = it.images - image
        it.copy(
          images = newImages,
          coverImageUri = if (it.coverImageUri == image.uri) {
            newImages.firstOrNull()?.uri
          } else it.coverImageUri
        )
      }
    }
  }

  private fun toggleOfficeSearch(active: Boolean) {
    _state.update {
      it.copy(
        isOfficeSearchActive = active,
        officeSearchQuery = if (!active) {
          it.selectedOffice?.name ?: ""
        } else it.officeSearchQuery
      )
    }
  }

  private fun selectOffice(office: Office) {
    _state.update { it.copy(
      officeId = office.id,
      selectedOffice = office,
      officeSearchQuery = office.name,
      isOfficeSearchActive = false
    ) }
  }

  @OptIn(DelicateCoroutinesApi::class)
  override fun onCleared() {
    super.onCleared()
    val s = _state.value

    if (!s.isUploadSuccess && !s.isDeleteSuccess && newlyAddedLocalUris.isNotEmpty()) {
      GlobalScope.launch {
        try {
          discardLocalImages(newlyAddedLocalUris)
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
    }
  }
}