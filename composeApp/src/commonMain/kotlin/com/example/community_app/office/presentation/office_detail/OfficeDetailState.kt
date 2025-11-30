package com.example.community_app.office.presentation.office_detail

import com.example.community_app.appointment.domain.Slot
import com.example.community_app.core.presentation.helpers.UiText
import com.example.community_app.office.domain.Office

data class OfficeDetailState(
  val isLoading: Boolean = false,
  val office: Office? = null,

  val selectedDateMillis: Long = 0L,
  val visibleSlots: List<Slot> = emptyList(),
  val isLoadingSlots: Boolean = false,

  val selectedSlot: Slot? = null,
  val isBooking: Boolean = false,
  val bookingSuccess: Boolean = false,
  val isUserLoggedIn: Boolean = false,

  val errorMessage: UiText? = null
)