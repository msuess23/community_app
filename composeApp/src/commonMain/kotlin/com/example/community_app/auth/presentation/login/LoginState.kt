package com.example.community_app.auth.presentation.login

import com.example.community_app.core.presentation.helpers.UiText

data class LoginState(
  val email: String = "citizen1@demo.local", // TODO: for prod and prof
  val password: String = "password!123", // TODO: for prod and prof
  val isPasswordVisible: Boolean = false,
  val isRememberMeChecked: Boolean = true,
  val isLoading: Boolean = false,
  val errorMessage: UiText? = null,
  val isLoginSuccessful: Boolean = false
)
