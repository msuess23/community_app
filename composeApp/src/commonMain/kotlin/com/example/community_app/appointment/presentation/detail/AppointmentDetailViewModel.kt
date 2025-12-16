package com.example.community_app.appointment.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.community_app.app.navigation.Route
import com.example.community_app.appointment.domain.usecase.CancelAppointmentUseCase
import com.example.community_app.appointment.domain.usecase.GetAppointmentDetailsUseCase
import com.example.community_app.core.domain.Result
import com.example.community_app.core.presentation.helpers.UiText
import com.example.community_app.core.presentation.helpers.toUiText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppointmentDetailViewModel(
  savedStateHandle: SavedStateHandle,
  private val getAppointmentDetails: GetAppointmentDetailsUseCase,
  private val cancelAppointment: CancelAppointmentUseCase
) : ViewModel() {

  private val appointmentId = savedStateHandle.toRoute<Route.AppointmentDetail>().id

  private val _isCancelling = MutableStateFlow(false)
  private val _showCancelDialog = MutableStateFlow(false)
  private val _actionError = MutableStateFlow<UiText?>(null)

  val state = combine(
    getAppointmentDetails(appointmentId),
    _isCancelling,
    _showCancelDialog,
    _actionError
  ) { result, isCancelling, showCancelDialog, actionError ->

    val details = (result as? Result.Success)?.data
    val loadingError = (result as? Result.Error)?.error?.toUiText()

    AppointmentDetailState(
      isLoading = result !is Result.Success && result !is Result.Error,
      appointment = details?.appointment,
      office = details?.office,
      isCancelling = isCancelling,
      isCancelled = result is Result.Error && details == null && !isCancelling,
      showCancelDialog = showCancelDialog,
      errorMessage = actionError ?: loadingError
    )
  }.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5000),
    AppointmentDetailState(isLoading = true)
  )

  fun onAction(action: AppointmentDetailAction) {
    when(action) {
      AppointmentDetailAction.OnCancelClick -> _showCancelDialog.value = true
      AppointmentDetailAction.OnDismissDialog -> _showCancelDialog.value = false
      AppointmentDetailAction.OnCancelConfirm -> cancel()
      else -> Unit
    }
  }

  private fun cancel() {
    viewModelScope.launch {
      _showCancelDialog.value = false
      _isCancelling.value = true
      _actionError.value = null

      val result = cancelAppointment(appointmentId)

      if (result is Result.Error) {
        _actionError.value = result.error.toUiText()
        _isCancelling.value = false
      }
    }
  }
}