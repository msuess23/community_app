package com.example.community_app.auth.presentation.login

sealed interface LoginAction {
  data class OnEmailChange(val email: String) : LoginAction
  data class OnPasswordChange(val password: String) : LoginAction
  data class OnRememberMeChange(val isChecked: Boolean) : LoginAction
  data object OnTogglePasswordVisibility : LoginAction
  data object OnLoginClick : LoginAction
  data object OnRegisterClick : LoginAction
  data object OnGuestClick : LoginAction
}