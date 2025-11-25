package com.example.community_app.auth.presentation.login

import com.example.community_app.core.presentation.helpers.UiText

data class LoginState(
  val email: String = "",
  val password: String = "",
  val isPasswordVisible: Boolean = false,
  val isRememberMeChecked: Boolean = true,
  val isLoading: Boolean = false,
  val errorMessage: UiText? = null,
  val isLoginSuccessful: Boolean = false
)
