package com.example.community_app.appointment.presentation.master

import com.example.community_app.appointment.domain.Appointment

sealed interface AppointmentMasterAction {
  data object OnRefresh : AppointmentMasterAction
  data class OnAppointmentClick(val appointment: Appointment) : AppointmentMasterAction
  data object OnLoginClick : AppointmentMasterAction
}