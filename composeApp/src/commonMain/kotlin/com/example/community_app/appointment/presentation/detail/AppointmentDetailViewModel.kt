package com.example.community_app.appointment.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.community_app.app.navigation.Route
import com.example.community_app.appointment.domain.usecase.CancelAppointmentUseCase
import com.example.community_app.appointment.domain.usecase.GetAppointmentDetailsUseCase
import com.example.community_app.core.domain.Result
import com.example.community_app.core.presentation.helpers.toUiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppointmentDetailViewModel(
  savedStateHandle: SavedStateHandle,
  private val getAppointmentDetails: GetAppointmentDetailsUseCase,
  private val cancelAppointment: CancelAppointmentUseCase
) : ViewModel() {

  private val appointmentId = savedStateHandle.toRoute<Route.AppointmentDetail>().id
  private val _state = MutableStateFlow(AppointmentDetailState())

  val state = _state.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5000),
    AppointmentDetailState()
  )

  init {
    loadData()
  }

  fun onAction(action: AppointmentDetailAction) {
    when(action) {
      AppointmentDetailAction.OnCancelClick -> _state.update { it.copy(showCancelDialog = true) }
      AppointmentDetailAction.OnDismissDialog -> _state.update { it.copy(showCancelDialog = false) }
      AppointmentDetailAction.OnCancelConfirm -> cancel()
      else -> Unit
    }
  }

  private fun loadData() {
    viewModelScope.launch {
      _state.update { it.copy(isLoading = true, errorMessage = null) }
      when(val result = getAppointmentDetails(appointmentId)) {
        is Result.Success -> {
          _state.update { it.copy(
            isLoading = false,
            appointment = result.data.appointment,
            office = result.data.office
          ) }
        }
        is Result.Error -> {
          _state.update { it.copy(isLoading = false, errorMessage = result.error.toUiText()) }
        }
      }
    }
  }

  private fun cancel() {
    viewModelScope.launch {
      _state.update { it.copy(showCancelDialog = false, isCancelling = true) }
      val result = cancelAppointment(appointmentId)
      if (result is Result.Success) {
        _state.update { it.copy(isCancelling = false, isCancelled = true) }
      } else {
        _state.update { it.copy(
          isCancelling = false,
          errorMessage = (result as Result.Error).error.toUiText()
        ) }
      }
    }
  }
}