package com.example.community_app.appointment.presentation.master

import com.example.community_app.appointment.domain.model.Appointment

sealed interface AppointmentMasterAction {
  data object OnRefresh : AppointmentMasterAction
  data class OnAppointmentClick(val appointment: Appointment) : AppointmentMasterAction
  data object OnLoginClick : AppointmentMasterAction

  data object OnToggleFilterSheet : AppointmentMasterAction
  data class OnStartDateSelect(val date: Long?) : AppointmentMasterAction
  data class OnEndDateSelect(val date: Long?) : AppointmentMasterAction
  data class OnSortChange(val option: AppointmentSortOption) : AppointmentMasterAction
  data object OnClearFilters : AppointmentMasterAction
}