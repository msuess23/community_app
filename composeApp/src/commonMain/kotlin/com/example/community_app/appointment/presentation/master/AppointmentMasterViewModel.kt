package com.example.community_app.appointment.presentation.master

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.community_app.appointment.domain.usecase.ObserveAppointmentsUseCase
import com.example.community_app.appointment.domain.usecase.ScheduleAppointmentRemindersUseCase
import com.example.community_app.auth.domain.usecase.IsUserLoggedInUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppointmentMasterViewModel(
  private val observeAppointments: ObserveAppointmentsUseCase,
  private val isUserLoggedIn: IsUserLoggedInUseCase,
  private val scheduleAppointmentReminders: ScheduleAppointmentRemindersUseCase
) : ViewModel() {

  private val _isRefreshing = MutableStateFlow(false)

  val state = combine(
    observeAppointments(),
    _isRefreshing,
    isUserLoggedIn()
  ) { appointments, refreshing, loggedIn ->
    AppointmentMasterState(
      appointments = appointments.sortedBy { it.startsAt },
      isLoading = refreshing,
      isUserLoggedIn = loggedIn
    )
  }.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5000L),
    AppointmentMasterState()
  )

  init {
    refresh()

    viewModelScope.launch { scheduleAppointmentReminders() }
  }

  fun onAction(action: AppointmentMasterAction) {
    when(action) {
      AppointmentMasterAction.OnRefresh -> refresh()
      else -> Unit
    }
  }

  private fun refresh() {
    viewModelScope.launch {
      _isRefreshing.value = true
      observeAppointments.sync()
      _isRefreshing.value = false
    }
  }
}