package com.example.community_app.auth.presentation.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.community_app.auth.domain.AuthRepository
import com.example.community_app.core.domain.onError
import com.example.community_app.core.domain.onSuccess
import com.example.community_app.core.presentation.helpers.UiText
import com.example.community_app.core.presentation.helpers.toUiText
import com.example.community_app.dto.RegisterDto
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.auth_validation_email_not_empty
import community_app.composeapp.generated.resources.auth_validation_password_length
import community_app.composeapp.generated.resources.auth_validation_password_not_matching
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel(
  private val authRepository: AuthRepository
) : ViewModel() {
  private val _state = MutableStateFlow(RegisterState())

  val state = _state.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5000L),
    RegisterState()
  )

  fun onAction(action: RegisterAction) {
    when (action) {
      is RegisterAction.OnDisplayNameChange -> {
        _state.update { it.copy(displayName = action.name) }
      }
      is RegisterAction.OnEmailChange -> {
        _state.update { it.copy(email = action.email) }
      }
      is RegisterAction.OnPasswordChange -> {
        _state.update { it.copy(password = action.password) }
      }
      is RegisterAction.OnPasswordRepeatChange -> {
        _state.update { it.copy(passwordRepeat = action.password) }
      }
      is RegisterAction.OnTogglePasswordVisibility -> {
        _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
      }
      is RegisterAction.OnRegisterClick -> {
        performRegister()
      }
      else -> Unit
    }
  }

  private fun performRegister() {
    val currentState = _state.value
    val email = currentState.email.trim()
    val password = currentState.password
    val repeat = currentState.passwordRepeat
    val name = currentState.displayName.trim().ifBlank { null }

    if (email.isBlank()) {
      _state.update { it.copy(
        errorMessage = UiText.StringResourceId(Res.string.auth_validation_email_not_empty)
      ) }
      return
    }
    if (password.length < 8) {
      _state.update { it.copy(
        errorMessage = UiText.StringResourceId(Res.string.auth_validation_password_length)
      ) }
      return
    }
    if (password != repeat) {
      _state.update { it.copy(
        errorMessage = UiText.StringResourceId(Res.string.auth_validation_password_not_matching)
      ) }
      return
    }

    viewModelScope.launch {
      _state.update { it.copy(isLoading = true, errorMessage = null) }

      authRepository.register(
        RegisterDto(
          email = email,
          password = password,
          displayName = name
        )
      )
        .onSuccess {
          _state.update { it.copy(
            isLoading = false,
            isRegisterSuccessful = true
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