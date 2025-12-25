package com.example.community_app.appointment.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.community_app.app.navigation.Route
import com.example.community_app.appointment.domain.model.AppointmentNote
import com.example.community_app.appointment.domain.usecase.detail.CancelAppointmentUseCase
import com.example.community_app.appointment.domain.usecase.detail.GetAppointmentDetailsUseCase
import com.example.community_app.appointment.domain.usecase.note.AddAppointmentNoteUseCase
import com.example.community_app.appointment.domain.usecase.note.DeleteAppointmentNoteUseCase
import com.example.community_app.appointment.domain.usecase.note.GetAppointmentNotesUseCase
import com.example.community_app.appointment.domain.usecase.note.UpdateAppointmentNoteUseCase
import com.example.community_app.core.domain.Result
import com.example.community_app.core.presentation.helpers.UiText
import com.example.community_app.core.presentation.helpers.toUiText
import com.example.community_app.core.util.parseIsoToMillis
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppointmentDetailViewModel(
  savedStateHandle: SavedStateHandle,
  private val getAppointmentDetails: GetAppointmentDetailsUseCase,
  private val cancelAppointment: CancelAppointmentUseCase,
  private val getAppointmentNotes: GetAppointmentNotesUseCase,
  private val addAppointmentNote: AddAppointmentNoteUseCase,
  private val updateAppointmentNote: UpdateAppointmentNoteUseCase,
  private val deleteAppointmentNote: DeleteAppointmentNoteUseCase
) : ViewModel() {

  private val appointmentId = savedStateHandle.toRoute<Route.AppointmentDetail>().id

  private val _isCancelling = MutableStateFlow(false)
  private val _isCancelSuccess = MutableStateFlow(false)
  private val _showCancelDialog = MutableStateFlow(false)
  private val _actionError = MutableStateFlow<UiText?>(null)

  private val _isNoteDialogVisible = MutableStateFlow(false)
  private val _editingNote = MutableStateFlow<AppointmentNote?>(null)

  private val appointmentFlow = getAppointmentDetails(appointmentId)
  private val notesFlow = getAppointmentNotes(appointmentId)

  private data class CancelUiState(
    val isCancelling: Boolean,
    val isCancelSuccess: Boolean,
    val showCancelDialog: Boolean,
    val error: UiText?
  )

  private val cancelStateFlow = combine(
    _isCancelling,
    _isCancelSuccess,
    _showCancelDialog,
    _actionError
  ) { cancelling, success, dialog, error ->
    CancelUiState(cancelling, success, dialog, error)
  }

  private data class NoteUiState(
    val isNoteDialogVisible: Boolean,
    val editingNote: AppointmentNote?
  )

  private val noteStateFlow = combine(
    _isNoteDialogVisible,
    _editingNote
  ) { isNoteDialogVisible, editingNote ->
    NoteUiState(isNoteDialogVisible, editingNote)
  }

  val state = combine(
    appointmentFlow,
    notesFlow,
    cancelStateFlow,
    noteStateFlow
  ) { result, notes, cancelState, noteState ->

    val details = (result as? Result.Success)?.data
    val loadingError = (result as? Result.Error)?.error?.toUiText()

    AppointmentDetailState(
      isLoading = result !is Result.Success && result !is Result.Error,
      appointment = details?.appointment,
      office = details?.office,
      notes = notes,
      isNoteDialogVisible = noteState.isNoteDialogVisible,
      editingNote = noteState.editingNote,
      isCancelling = cancelState.isCancelling,
      isCancelSuccess = cancelState.isCancelSuccess,
      showCancelDialog = cancelState.showCancelDialog,
      errorMessage = cancelState.error ?: loadingError
    )
  }.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5000),
    AppointmentDetailState(isLoading = true)
  )

  fun onAction(action: AppointmentDetailAction) {
    when(action) {
      AppointmentDetailAction.OnCancelClick -> _showCancelDialog.value = true
      AppointmentDetailAction.OnDismissDialog -> _showCancelDialog.value = false
      AppointmentDetailAction.OnCancelConfirm -> cancel()

      AppointmentDetailAction.OnAddNoteClick -> {
        _editingNote.value = null
        _isNoteDialogVisible.value = true
      }
      is AppointmentDetailAction.OnEditNoteClick -> {
        _editingNote.value = action.note
        _isNoteDialogVisible.value = true
      }
      is AppointmentDetailAction.OnDeleteNoteClick -> {
        viewModelScope.launch {
          deleteAppointmentNote(action.noteId)
        }
      }
      AppointmentDetailAction.OnCloseNoteDialog -> {
        _isNoteDialogVisible.value = false
        _editingNote.value = null
      }
      is AppointmentDetailAction.OnSubmitNote -> submitNote(action.text)

      else -> Unit
    }
  }

  private fun submitNote(text: String) {
    viewModelScope.launch {
      val currentEditingNote = _editingNote.value

      if (currentEditingNote != null) {
        updateAppointmentNote(currentEditingNote.id, text)
      } else {
        val currentDetails = (state.value.appointment)

        if (currentDetails != null) {
          val appointmentDate = parseIsoToMillis(currentDetails.endsAt)

          addAppointmentNote(
            appointmentId = appointmentId,
            appointmentDate = appointmentDate,
            text = text
          )
        }
      }

      _isNoteDialogVisible.value = false
      _editingNote.value = null
    }
  }

  private fun cancel() {
    viewModelScope.launch {
      _showCancelDialog.value = false
      _isCancelling.value = true
      _actionError.value = null

      val result = cancelAppointment(appointmentId)

      if (result is Result.Error) {
        _actionError.value = result.error.toUiText()
        _isCancelling.value = false
      } else {
        _isCancelSuccess.value = true
        _isCancelling.value = false
      }
    }
  }
}