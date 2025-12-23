package com.example.community_app.auth.presentation.forgot_password

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.community_app.auth.domain.repository.AuthRepository
import com.example.community_app.core.domain.onError
import com.example.community_app.core.domain.onSuccess
import com.example.community_app.core.presentation.helpers.UiText
import com.example.community_app.core.presentation.helpers.toUiText
import community_app.composeapp.generated.resources.Res
import community_app.composeapp.generated.resources.auth_validation_email_not_empty
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ForgotPasswordViewModel(
  private val authRepository: AuthRepository
) : ViewModel() {
  private val _state = MutableStateFlow(ForgotPasswordState())

  val state = _state.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5000L),
    ForgotPasswordState()
  )

  fun onAction(action: ForgotPasswordAction) {
    when(action) {
      is ForgotPasswordAction.OnEmailChange -> {
        _state.update { it.copy(email = action.email) }
      }
      is ForgotPasswordAction.OnSubmitClick -> {
        sendResetMail()
      }
      is ForgotPasswordAction.OnDialogDismiss -> {
        _state.update { it.copy(showSuccessDialog = false) }
      }
      else -> Unit
    }
  }

  private fun sendResetMail() {
    val email = _state.value.email.trim()

    if (email.isBlank()) {
      _state.update { it.copy(
        errorMessage = UiText.StringResourceId(Res.string.auth_validation_email_not_empty)
      ) }
      return
    }

    viewModelScope.launch {
      _state.update { it.copy(
        isLoading = true,
        errorMessage = null
      ) }

      authRepository.forgotPassword(email)
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