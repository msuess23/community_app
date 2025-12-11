package com.example.community_app.office.presentation.office_detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.community_app.app.navigation.Route
import com.example.community_app.appointment.domain.AppointmentRepository
import com.example.community_app.appointment.domain.Slot
import com.example.community_app.appointment.domain.usecase.BookAppointmentUseCase
import com.example.community_app.appointment.domain.usecase.GetFreeSlotsUseCase
import com.example.community_app.auth.domain.usecase.IsUserLoggedInUseCase
import com.example.community_app.core.domain.Result
import com.example.community_app.core.domain.permission.CalendarPermissionService
import com.example.community_app.core.domain.permission.PermissionStatus
import com.example.community_app.core.presentation.helpers.UiText
import com.example.community_app.core.presentation.helpers.toUiText
import com.example.community_app.core.util.addDays
import com.example.community_app.core.util.formatIsoDate
import com.example.community_app.core.util.formatMillisDate
import com.example.community_app.core.util.getCurrentTimeMillis
import com.example.community_app.core.util.getStartOfDay
import com.example.community_app.core.util.parseIsoToMillis
import com.example.community_app.core.util.toIso8601
import com.example.community_app.office.domain.OfficeRepository
import com.example.community_app.settings.domain.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.max

class OfficeDetailViewModel(
  savedStateHandle: SavedStateHandle,
  private val officeRepository: OfficeRepository,
  private val appointmentRepository: AppointmentRepository,
  private val settingsRepository: SettingsRepository,
  private val calendarPermissionService: CalendarPermissionService,
  private val getFreeSlotsUseCase: GetFreeSlotsUseCase,
  private val isUserLoggedInUseCase: IsUserLoggedInUseCase,
  private val bookAppointmentUseCase: BookAppointmentUseCase
) : ViewModel() {
  private val officeId = savedStateHandle.toRoute<Route.OfficeDetail>().id

  private val daysInFuture = 90

  // State-Inputs
  private val _selectedDate = MutableStateFlow(getStartOfDay(getCurrentTimeMillis()))
  private val _allSlots = MutableStateFlow<List<Slot>>(emptyList())
  private val _uiState = MutableStateFlow(UiState())

  val state = combine(
    officeRepository.getOffice(officeId),
    _selectedDate,
    _allSlots,
    _uiState,
    isUserLoggedInUseCase()
  ) { office, selectedDate, allSlots, ui, isLoggedIn ->
    val nextDay = addDays(selectedDate, 1)

    val today = getStartOfDay(getCurrentTimeMillis())
    val maxDate = addDays(today, daysInFuture)

    val filteredSlots = allSlots.filter { slot ->
      val slotStart = parseIsoToMillis(slot.startIso)
      slotStart in selectedDate until nextDay
    }.sortedBy { it.startIso }

    OfficeDetailState(
      isLoading = ui.isLoadingOffice,
      office = office,
      selectedDateMillis = selectedDate,
      selectableDateRange = today..maxDate,
      visibleSlots = filteredSlots,
      isLoadingSlots = ui.isLoadingSlots,
      selectedSlot = ui.selectedSlot,
      isBooking = ui.isBooking,
      bookingSuccess = ui.bookingSuccess,
      errorMessage = ui.errorMessage,
      isUserLoggedIn = isLoggedIn,
      showDatePicker = ui.showDatePicker,
      hasCalendarPermission = ui.hasCalendarPermission,
      shouldAddToCalendar = ui.shouldAddToCalendar
    )
  }.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5000L),
    OfficeDetailState()
  )

  init {
    loadOffice()
    loadAllSlots()
    checkCalendarSettings()
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
            errorMessage = UiText.DynamicString("Bitte melde Dich an, um einen Termin zu buchen."))
          } // TODO: Localize
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

  private fun loadOffice() {
    viewModelScope.launch {
      _uiState.update { it.copy(isLoadingOffice = true) }
      officeRepository.refreshOffice(officeId)
      _uiState.update { it.copy(isLoadingOffice = false) }
    }
  }

  private fun loadAllSlots() {
    viewModelScope.launch {
      _uiState.update { it.copy(isLoadingSlots = true) }

      val nowMillis = getCurrentTimeMillis()
      val endDateMillis = addDays(nowMillis, daysInFuture)

      val from = toIso8601(nowMillis)
      val to = toIso8601(endDateMillis)

      val result = getFreeSlotsUseCase(officeId, from, to)

      if (result is Result.Success) {
        _allSlots.value = result.data
      }

      _uiState.update { it.copy(isLoadingSlots = false) }
    }
  }

  private fun performBooking() {
    val slot = _uiState.value.selectedSlot ?: return
    val addToCalendar = _uiState.value.shouldAddToCalendar

    viewModelScope.launch {
      _uiState.update { it.copy(isBooking = true) }
      val result = bookAppointmentUseCase(officeId, slot.id, addToCalendar)

      if (result is Result.Success) {
        _allSlots.update { currentList ->
          currentList.filter { it.id != slot.id }
        }

        _uiState.update { it.copy(isBooking = false, bookingSuccess = true, selectedSlot = null) }
      } else {
        val error = (result as Result.Error).error
        _uiState.update { it.copy(isBooking = false, errorMessage = error.toUiText()) }
      }
    }
  }

  private fun checkCalendarSettings() {
    viewModelScope.launch {
      val settings = settingsRepository.settings.first()
      val permission = calendarPermissionService.checkPermission()

      val hasPermission = permission == PermissionStatus.GRANTED

      _uiState.update { it.copy(
        hasCalendarPermission = hasPermission,
        shouldAddToCalendar = hasPermission && settings.calendarSyncEnabled
      ) }
    }
  }

  private data class UiState(
    val isLoadingOffice: Boolean = false,
    val isLoadingSlots: Boolean = false,
    val selectedSlot: Slot? = null,
    val isBooking: Boolean = false,
    val bookingSuccess: Boolean = false,
    val showDatePicker: Boolean = false,
    val hasCalendarPermission: Boolean = false,
    val shouldAddToCalendar: Boolean = false,
    val errorMessage: UiText? = null
  )
}