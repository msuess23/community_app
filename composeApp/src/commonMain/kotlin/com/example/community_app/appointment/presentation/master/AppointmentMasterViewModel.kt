package com.example.community_app.appointment.presentation.master

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.community_app.appointment.domain.usecase.master.ObserveAppointmentsUseCase
import com.example.community_app.appointment.domain.usecase.detail.ScheduleAppointmentRemindersUseCase
import com.example.community_app.appointment.domain.usecase.master.FilterAppointmentsUseCase
import com.example.community_app.core.presentation.helpers.toUiText
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppointmentMasterViewModel(
  private val observeAppointments: ObserveAppointmentsUseCase,
  private val scheduleAppointmentReminders: ScheduleAppointmentRemindersUseCase,
  private val filterAppointments: FilterAppointmentsUseCase
) : ViewModel() {
  private val _filterState = MutableStateFlow(AppointmentFilterState())
  private val _isFilterSheetVisible = MutableStateFlow(false)
  private val _forceRefreshTrigger = MutableStateFlow(false)

  init { viewModelScope.launch { scheduleAppointmentReminders() } }

  @OptIn(ExperimentalCoroutinesApi::class)
  private val appointmentDataFlow = _forceRefreshTrigger.flatMapLatest { force ->
    observeAppointments(forceRefresh = force)
  }

  val state = combine(
    appointmentDataFlow,
    _filterState,
    _isFilterSheetVisible,
  ) { dataResult, filter, isFilterVisible ->
    if (_forceRefreshTrigger.value && !dataResult.syncStatus.isLoading) {
      _forceRefreshTrigger.value = false
    }

    val filteredAppointments = filterAppointments(
      appointments = dataResult.appointments,
      filter = filter
    )

    AppointmentMasterState(
      appointments = filteredAppointments,
      isLoading = dataResult.syncStatus.isLoading,
      errorMessage = dataResult.syncStatus.error?.toUiText(),
      filter = filter,
      isFilterSheetVisible = isFilterVisible
    )
  }.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5000L),
    AppointmentMasterState(isLoading = true)
  )

  fun onAction(action: AppointmentMasterAction) {
    when(action) {
      AppointmentMasterAction.OnRefresh -> _forceRefreshTrigger.value = true
      AppointmentMasterAction.OnToggleFilterSheet -> _isFilterSheetVisible.update { !it }
      is AppointmentMasterAction.OnSortChange -> _filterState.update { it.copy(sortOption = action.option) }
      is AppointmentMasterAction.OnStartDateSelect -> _filterState.update { it.copy(startDate = action.date) }
      is AppointmentMasterAction.OnEndDateSelect -> _filterState.update { it.copy(endDate = action.date) }
      AppointmentMasterAction.OnClearFilters -> _filterState.update { AppointmentFilterState() }
      else -> Unit
    }
  }
}