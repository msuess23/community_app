package com.example.community_app.appointment.presentation.detail

import com.example.community_app.appointment.domain.model.Appointment
import com.example.community_app.core.presentation.helpers.UiText
import com.example.community_app.office.domain.model.Office

data class AppointmentDetailState(
  val isLoading: Boolean = true,
  val appointment: Appointment? = null,
  val office: Office? = null,

  val isCancelling: Boolean = false,
  val isCancelled: Boolean = false,
  val showCancelDialog: Boolean = false,

  val errorMessage: UiText? = null
)