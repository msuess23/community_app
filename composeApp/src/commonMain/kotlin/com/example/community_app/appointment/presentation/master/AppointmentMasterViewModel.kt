package com.example.community_app.appointment.presentation.master

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.community_app.appointment.domain.usecase.master.ObserveAppointmentsUseCase
import com.example.community_app.appointment.domain.usecase.detail.ScheduleAppointmentRemindersUseCase
import com.example.community_app.appointment.domain.usecase.master.FilterAppointmentsUseCase
import com.example.community_app.core.presentation.helpers.toUiText
import com.example.community_app.info.presentation.info_master.InfoFilterState
import com.example.community_app.info.presentation.info_master.InfoMasterViewModel.Inputs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
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

  private val inputs = combine(
    _filterState,
    _forceRefreshTrigger,
    _isFilterSheetVisible
  ) { filter, forceRefresh, isFilterVisible ->
    Inputs(filter, forceRefresh, isFilterVisible)
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  val state = inputs.flatMapLatest { inputs ->
    observeAppointments(forceRefresh = inputs.forceRefresh).map { result ->
      val filtered = filterAppointments(
        appointments = result.appointments,
        filter = inputs.filter
      )

      if (inputs.forceRefresh && !result.syncStatus.isLoading) {
        _forceRefreshTrigger.value = false
      }

      AppointmentMasterState(
        appointments = filtered,
        isLoading = result.syncStatus.isLoading,
        errorMessage = result.syncStatus.error?.toUiText(),
        filter = inputs.filter,
        isFilterSheetVisible = inputs.isFilterVisible
      )
    }
  }.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5000L),
    AppointmentMasterState(isLoading = true)
  )

  init {
    viewModelScope.launch { scheduleAppointmentReminders() }
  }

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

  private data class Inputs(
    val filter: AppointmentFilterState,
    val forceRefresh: Boolean,
    val isFilterVisible: Boolean
  )
}