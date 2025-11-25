package com.example.community_app.auth.presentation.register

import com.example.community_app.auth.presentation.login.LoginAction

sealed interface RegisterAction {
  data class OnEmailChange(val email: String) : RegisterAction
  data class OnPasswordChange(val password: String) : RegisterAction
  data class OnPasswordRepeatChange(val password: String) : RegisterAction
  data class OnDisplayNameChange(val name: String) : RegisterAction

  data object OnTogglePasswordVisibility : RegisterAction

  data object OnRegisterClick : RegisterAction
  data object OnLoginClick : RegisterAction
  data object OnGuestClick : RegisterAction
}
