package com.example.community_app.appointment.presentation.detail

sealed interface AppointmentDetailAction {
  data object OnNavigateBack : AppointmentDetailAction
  data object OnCancelClick : AppointmentDetailAction
  data object OnCancelConfirm : AppointmentDetailAction
  data object OnDismissDialog : AppointmentDetailAction
}