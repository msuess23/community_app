package com.example.community_app.office.presentation.office_detail

import com.example.community_app.appointment.domain.model.Slot
import com.example.community_app.core.presentation.helpers.UiText
import com.example.community_app.office.domain.model.Office

data class OfficeDetailState(
  val isLoading: Boolean = false,
  val office: Office? = null,

  val selectedDateMillis: Long = 0L,
  val selectableDateRange: LongRange = LongRange.EMPTY,
  val visibleSlots: List<Slot> = emptyList(),
  val isLoadingSlots: Boolean = false,

  val shouldAddToCalendar: Boolean = false,
  val hasCalendarPermission: Boolean = false,

  val selectedSlot: Slot? = null,
  val isBooking: Boolean = false,
  val bookingSuccess: Boolean = false,
  val isUserLoggedIn: Boolean = false,
  val showDatePicker: Boolean = false,

  val infoMessage: UiText? = null,
  val errorMessage: UiText? = null
)