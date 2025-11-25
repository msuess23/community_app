package com.example.community_app.auth.presentation.register

import com.example.community_app.core.presentation.helpers.UiText

data class RegisterState(
  val email: String = "",
  val password: String = "",
  val passwordRepeat: String = "",
  val displayName: String = "",

  val isPasswordVisible: Boolean = false,

  val isLoading: Boolean = false,
  val errorMessage: UiText? = null,
  val isRegisterSuccessful: Boolean = false
)
