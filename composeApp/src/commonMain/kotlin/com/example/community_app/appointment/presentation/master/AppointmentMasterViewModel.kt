package com.example.community_app.appointment.presentation.master

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.community_app.appointment.domain.usecase.ObserveAppointmentsUseCase
import com.example.community_app.appointment.domain.usecase.ScheduleAppointmentRemindersUseCase
import com.example.community_app.core.presentation.helpers.toUiText
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppointmentMasterViewModel(
  private val observeAppointments: ObserveAppointmentsUseCase,
  private val scheduleAppointmentReminders: ScheduleAppointmentRemindersUseCase
) : ViewModel() {

  private val _forceRefreshTrigger = MutableStateFlow(false)

  @OptIn(ExperimentalCoroutinesApi::class)
  val state = _forceRefreshTrigger.flatMapLatest { forceRefresh ->
    observeAppointments(forceRefresh = forceRefresh).map { result ->
      if (forceRefresh && !result.syncStatus.isLoading) {
        _forceRefreshTrigger.value = false
      }

      AppointmentMasterState(
        appointments = result.appointments.sortedBy { it.startsAt },
        isLoading = result.syncStatus.isLoading,
        errorMessage = result.syncStatus.error?.toUiText()
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
      else -> Unit
    }
  }
}