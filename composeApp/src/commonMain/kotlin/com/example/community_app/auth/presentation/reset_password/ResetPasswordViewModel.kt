package com.example.community_app.auth.presentation.reset_password

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.community_app.app.navigation.Route
import com.example.community_app.auth.domain.AuthRepository
import com.example.community_app.core.domain.onError
import com.example.community_app.core.domain.onSuccess
import com.example.community_app.core.presentation.helpers.UiText
import com.example.community_app.core.presentation.helpers.toUiText
import com.example.community_app.dto.ResetPasswordRequest
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.auth_validation_password_not_matching
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ResetPasswordViewModel(
  savedStateHandle: SavedStateHandle,
  private val authRepository: AuthRepository
) : ViewModel() {
  private val email = savedStateHandle.toRoute<Route.ResetPassword>().email

  private val _state = MutableStateFlow(ResetPasswordState())
  val state = _state.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5000),
    ResetPasswordState()
  )

  fun onAction(action: ResetPasswordAction) {
    when(action) {
      is ResetPasswordAction.OnOtpChange -> {
        _state.update { it.copy(otp = action.otp) }
      }
      is ResetPasswordAction.OnPasswordChange -> {
        _state.update { it.copy(password = action.password) }
      }
      is ResetPasswordAction.OnPasswordRepeatChange -> {
        _state.update { it.copy(passwordRepeat = action.password) }
      }
      is ResetPasswordAction.OnTogglePasswordVisibility -> {
        _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
      }
      is ResetPasswordAction.OnSubmit -> {
        submitReset()
      }
      else -> Unit
    }
  }

  private fun submitReset() {
    val currentState = _state.value
    val password = currentState.password
    val repeat = currentState.passwordRepeat
    val otp = currentState.otp

    if (password != repeat) {
      _state.update { it.copy(
        errorMessage = UiText.StringResourceId(Res.string.auth_validation_password_not_matching)
      ) }
      return
    }

    viewModelScope.launch {
      _state.update { it.copy(
        isLoading = true,
        errorMessage = null
      ) }

      val request = ResetPasswordRequest(
        email = email,
        otp = otp,
        newPassword = password
      )

      authRepository.resetPassword(request)
        .onSuccess {
          _state.update { it.copy(
            isLoading = false,
            showSuccessDialog = true
          ) }
        }
        .onError { error ->
          _state.update { it.copy(
            isLoading = false,
            errorMessage = error.toUiText()
          ) }
        }
    }
  }
}
