package com.example.community_app.appointment.presentation.master

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.community_app.appointment.domain.usecase.ObserveAppointmentsUseCase
import com.example.community_app.auth.domain.usecase.IsUserLoggedInUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppointmentMasterViewModel(
  private val observeAppointments: ObserveAppointmentsUseCase,
  isUserLoggedInUseCase: IsUserLoggedInUseCase
) : ViewModel() {

  private val _isRefreshing = MutableStateFlow(false)

  val state = combine(
    observeAppointments(),
    _isRefreshing,
    isUserLoggedInUseCase()
  ) { appointments, refreshing, loggedIn ->
    AppointmentMasterState(
      appointments = appointments.sortedBy { it.startsAt }, // Sortierung client-seitig sicherstellen
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