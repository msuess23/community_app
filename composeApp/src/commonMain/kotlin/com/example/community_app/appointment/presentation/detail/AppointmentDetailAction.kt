package com.example.community_app.appointment.presentation.detail

import com.example.community_app.appointment.domain.model.AppointmentNote

sealed interface AppointmentDetailAction {
  data object OnNavigateBack : AppointmentDetailAction
  data object OnCancelClick : AppointmentDetailAction
  data object OnCancelConfirm : AppointmentDetailAction
  data object OnDismissDialog : AppointmentDetailAction

  data object OnAddNoteClick : AppointmentDetailAction
  data class OnEditNoteClick(val note: AppointmentNote) : AppointmentDetailAction
  data class OnDeleteNoteClick(val noteId: Int) : AppointmentDetailAction
  data object OnCloseNoteDialog : AppointmentDetailAction
  data class OnSubmitNote(val text: String) : AppointmentDetailAction
}