package com.example.community_app.appointment.presentation.master

import com.example.community_app.appointment.domain.Appointment
import com.example.community_app.core.presentation.helpers.UiText

data class AppointmentMasterState(
  val appointments: List<Appointment> = emptyList(),
  val isLoading: Boolean = false,
  val isUserLoggedIn: Boolean = false,
  val errorMessage: UiText? = null
)