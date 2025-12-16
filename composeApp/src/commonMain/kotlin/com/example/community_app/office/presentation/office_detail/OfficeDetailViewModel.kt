package com.example.community_app.office.presentation.office_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.community_app.app.navigation.Route
import com.example.community_app.appointment.domain.Slot
import com.example.community_app.appointment.domain.usecase.BookAppointmentUseCase
import com.example.community_app.auth.domain.usecase.IsUserLoggedInUseCase
import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.calendar.usecase.GetCalendarSyncStateUseCase
import com.example.community_app.core.presentation.helpers.UiText
import com.example.community_app.core.presentation.helpers.toUiText
import com.example.community_app.core.presentation.state.SyncStatus
import com.example.community_app.core.util.addDays
import com.example.community_app.core.util.getCurrentTimeMillis
import com.example.community_app.core.util.getStartOfDay
import com.example.community_app.office.domain.usecase.FilterSlotsUseCase
import com.example.community_app.office.domain.usecase.GetOfficeDetailUseCase
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.slot_booking_error
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OfficeDetailViewModel(
  savedStateHandle: SavedStateHandle,
  private val getOfficeDetail: GetOfficeDetailUseCase,
  private val filterSlots: FilterSlotsUseCase,
  private val getCalendarSyncState: GetCalendarSyncStateUseCase,
  private val isUserLoggedIn: IsUserLoggedInUseCase,
  private val bookAppointmentUseCase: BookAppointmentUseCase
) : ViewModel() {
  private val officeId = savedStateHandle.toRoute<Route.OfficeDetail>().id

  // State-Inputs
  private val _selectedDate = MutableStateFlow(getStartOfDay(getCurrentTimeMillis()))
  private val _uiState = MutableStateFlow(UiState())
  private val _bookedSlotIds = MutableStateFlow<Set<Int>>(emptySet())
  private val _bookingStatus = MutableStateFlow(SyncStatus(isLoading = false))

  private val detailFlow = getOfficeDetail(officeId)

  private data class LocalViewState(
    val selectedDate: Long,
    val ui: UiState,
    val bookingStatus: SyncStatus,
    val bookedIds: Set<Int>
  )

  private val localStateFlow = combine(
    _selectedDate,
    _uiState,
    _bookingStatus,
    _bookedSlotIds
  ) { selectedDate, ui, bookingStatus, bookedIds ->
    LocalViewState(selectedDate, ui, bookingStatus, bookedIds)
  }

  val state = combine(
    detailFlow,
    localStateFlow,
    isUserLoggedIn()
  ) { result, local, isLoggedIn ->
    val (selectedDate, ui, bookingStatus, bookedIds) = local

    val filteredSlots = filterSlots(
      allSlots = result.allSlots,
      selectedDateMillis = local.selectedDate
    ).filter { it.id !in bookedIds }

    val today = getStartOfDay(getCurrentTimeMillis())
    val maxDate = addDays(today, 90)

    val displayError = bookingStatus.error?.toUiText()
      ?: result.officeSyncStatus.error?.toUiText()
      ?: ui.errorMessage

    OfficeDetailState(
      isLoading = result.officeSyncStatus.isLoading,
      office = result.office,
      selectedDateMillis = local.selectedDate,
      selectableDateRange = today..maxDate,
      visibleSlots = filteredSlots,
      isLoadingSlots = result.slotsSyncStatus.isLoading,
      selectedSlot = local.ui.selectedSlot,
      isBooking = bookingStatus.isLoading,
      bookingSuccess = local.ui.bookingSuccess,
      errorMessage = displayError,
      infoMessage = result.slotsSyncStatus.error?.toUiText(),
      showDatePicker = local.ui.showDatePicker,
      hasCalendarPermission = local.ui.hasCalendarPermission,
      shouldAddToCalendar = local.ui.shouldAddToCalendar,
      isUserLoggedIn = isLoggedIn
    )
  }.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5000L),
    OfficeDetailState()
  )

  init {
    checkCalendarStatus()
  }

  fun onAction(action: OfficeDetailAction) {
    when(action) {
      is OfficeDetailAction.OnNextDayClick -> changeDate(1)
      is OfficeDetailAction.OnPreviousDayClick -> changeDate(-1)

      is OfficeDetailAction.OnCalendarClick -> {
        _uiState.update { it.copy(showDatePicker = true) }
      }
      is OfficeDetailAction.OnDismissDatePicker -> {
        _uiState.update { it.copy(showDatePicker = false) }
      }
      is OfficeDetailAction.OnDateSelected -> {
        updateDate(action.dateMillis)
        _uiState.update { it.copy(showDatePicker = false) }
      }

      is OfficeDetailAction.OnSlotClick -> {
        if (!state.value.isUserLoggedIn) {
          _uiState.update { it.copy(
            errorMessage = UiText.StringResourceId(Res.string.slot_booking_error)
          ) }
        } else {
          _uiState.update { it.copy(selectedSlot = action.slot) }
        }
      }

      is OfficeDetailAction.OnToggleCalendarExport -> {
        _uiState.update { it.copy(shouldAddToCalendar = !_uiState.value.shouldAddToCalendar) }
      }

      OfficeDetailAction.OnDismissBookingDialog -> _uiState.update { it.copy(selectedSlot = null) }

      OfficeDetailAction.OnConfirmBooking -> performBooking()

      else -> Unit
    }
  }

  private fun changeDate(days: Int) {
    val currentSelected = _selectedDate.value
    val newDate = addDays(currentSelected, days)

    if (newDate in state.value.selectableDateRange) {
      _selectedDate.value = newDate
    }
  }

  private fun updateDate(dateMillis: Long?) {
    if (dateMillis != null && dateMillis in state.value.selectableDateRange) {
      _selectedDate.value = dateMillis
    }
  }

  private fun checkCalendarStatus() {
    viewModelScope.launch {
      val status = getCalendarSyncState()
      _uiState.update { it.copy(
        hasCalendarPermission = status.hasPermission,
        shouldAddToCalendar = status.shouldAutoAdd
      )}
    }
  }

  private fun performBooking() {
    val slot = _uiState.value.selectedSlot ?: return
    val addToCalendar = _uiState.value.shouldAddToCalendar

    viewModelScope.launch {
      bookAppointmentUseCase(
        officeId = officeId,
        slotId = slot.id,
        addToCalendar = addToCalendar
      ).collect { status ->
        _bookingStatus.value = status

        if (!status.isLoading) {
          _uiState.update {
            it.copy(
              bookingSuccess = true,
              selectedSlot = null
            )
          }

          if (status.error == null || status.error == DataError.Local.CALENDAR_EXPORT_FAILED) {
            _bookedSlotIds.update { it + slot.id }
          }
        }
      }
    }
  }

  private data class UiState(
    val isLoadingOffice: Boolean = false,
    val isLoadingSlots: Boolean = false,
    val selectedDate: Long = getStartOfDay(getCurrentTimeMillis()),
    val selectedSlot: Slot? = null,
    val isBooking: Boolean = false,
    val bookingSuccess: Boolean = false,
    val showDatePicker: Boolean = false,
    val hasCalendarPermission: Boolean = false,
    val shouldAddToCalendar: Boolean = false,
    val errorMessage: UiText? = null
  )
}