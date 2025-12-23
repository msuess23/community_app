package com.example.community_app.auth.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.community_app.auth.domain.repository.AuthRepository
import com.example.community_app.core.domain.onError
import com.example.community_app.core.domain.onSuccess
import com.example.community_app.core.presentation.helpers.toUiText
import com.example.community_app.dto.LoginDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
  private val authRepository: AuthRepository
) : ViewModel() {
  private val _state = MutableStateFlow(LoginState())

  val state = _state.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5000L),
    LoginState()
  )

  fun onAction(action: LoginAction) {
    when (action) {
      is LoginAction.OnEmailChange -> {
        _state.update { it.copy(email = action.email) }
      }
      is LoginAction.OnPasswordChange -> {
        _state.update { it.copy(password = action.password) }
      }
      is LoginAction.OnRememberMeChange -> {
        _state.update { it.copy(isRememberMeChecked = action.isChecked) }
      }
      is LoginAction.OnTogglePasswordVisibility -> {
        _state.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }
      }
      is LoginAction.OnLoginClick -> {
        performLogin()
      }
      else -> Unit
    }
  }

  private fun performLogin() {
    val email = _state.value.email.trim()
    val password = _state.value.password

    if (email.isBlank() || password.isBlank()) return

    viewModelScope.launch {
      _state.update { it.copy(
        isLoading = true,
        errorMessage = null
      ) }

      authRepository.login(
        LoginDto(email = email, password = password)
      )
        .onSuccess {
          _state.update { it.copy(
            isLoading = false,
            isLoginSuccessful = true
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