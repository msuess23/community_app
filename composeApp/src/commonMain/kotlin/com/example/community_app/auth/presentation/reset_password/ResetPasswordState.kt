package com.example.community_app.auth.presentation.reset_password

import com.example.community_app.core.presentation.helpers.UiText

data class ResetPasswordState(
  val otp: String = "",
  val password: String = "",
  val passwordRepeat: String = "",
  val isPasswordVisible: Boolean = false,
  val isLoading: Boolean = false,
  val errorMessage: UiText? = null,
  val showSuccessDialog: Boolean = false
)