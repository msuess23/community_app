package com.example.community_app.appointment.presentation.master

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.community_app.appointment.domain.usecase.ObserveAppointmentsUseCase
import com.example.community_app.appointment.domain.usecase.ScheduleAppointmentRemindersUseCase
import com.example.community_app.core.domain.Result
import com.example.community_app.core.presentation.helpers.UiText
import com.example.community_app.core.presentation.helpers.toUiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppointmentMasterViewModel(
  private val observeAppointments: ObserveAppointmentsUseCase,
  private val scheduleAppointmentReminders: ScheduleAppointmentRemindersUseCase
) : ViewModel() {

  private val _uiControlState = MutableStateFlow(UiControlState())

  val state = combine(
    observeAppointments(),
    _uiControlState
  ) { appointments, uiControl ->
    AppointmentMasterState(
      appointments = appointments.sortedBy { it.startsAt },
      isLoading = uiControl.isLoading,
      errorMessage = uiControl.errorMessage
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
      _uiControlState.update { it.copy(isLoading = true, errorMessage = null) }

      when(val result = observeAppointments.sync()) {
        is Result.Success -> {
          _uiControlState.update { it.copy(isLoading = false) }
        }
        is Result.Error -> {
          _uiControlState.update { it.copy(
            isLoading = false,
            errorMessage = result.error.toUiText()
          ) }
        }
      }
    }
  }

  private data class UiControlState(
    val isLoading: Boolean = false,
    val errorMessage: UiText? = null
  )
}