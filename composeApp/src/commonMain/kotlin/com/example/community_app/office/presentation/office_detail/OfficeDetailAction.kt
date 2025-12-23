package com.example.community_app.office.presentation.office_detail

import com.example.community_app.appointment.domain.model.Slot

sealed interface OfficeDetailAction {
  data object OnNavigateBack : OfficeDetailAction

  data object OnNextDayClick : OfficeDetailAction
  data object OnPreviousDayClick : OfficeDetailAction
  data object OnCalendarClick : OfficeDetailAction
  data object OnDismissDatePicker : OfficeDetailAction
  data class OnDateSelected(val dateMillis: Long?) : OfficeDetailAction

  data class OnSlotClick(val slot: Slot) : OfficeDetailAction
  data object OnDismissBookingDialog : OfficeDetailAction
  data object OnConfirmBooking : OfficeDetailAction
  data class OnToggleCalendarExport(val enabled: Boolean) : OfficeDetailAction
  data object OnLoginRedirect : OfficeDetailAction
}