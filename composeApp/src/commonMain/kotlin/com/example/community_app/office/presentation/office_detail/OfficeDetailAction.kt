package com.example.community_app.office.presentation.office_detail

import com.example.community_app.appointment.domain.Slot

sealed interface OfficeDetailAction {
  data object OnNavigateBack : OfficeDetailAction

  data object OnNextDayClick : OfficeDetailAction
  data object OnPreviousDayClick : OfficeDetailAction

  data class OnSlotClick(val slot: Slot) : OfficeDetailAction
  data object OnDismissBookingDialog : OfficeDetailAction
  data object OnConfirmBooking : OfficeDetailAction
  data object OnLoginRedirect : OfficeDetailAction
}