package com.example.community_app.appointment.presentation.master

import com.example.community_app.appointment.domain.model.Appointment
import com.example.community_app.core.presentation.helpers.UiText

data class AppointmentMasterState(
  val appointments: List<Appointment> = emptyList(),
  val isLoading: Boolean = false,
  val errorMessage: UiText? = null,
  val isFilterSheetVisible: Boolean = false,
  val filter: AppointmentFilterState = AppointmentFilterState()
)

data class AppointmentFilterState(
  val startDate: Long? = null,
  val endDate: Long? = null,
  val sortOption: AppointmentSortOption = AppointmentSortOption.DATE_ASC
)

enum class AppointmentSortOption {
  DATE_ASC,
  DATE_DESC
}