package com.example.community_app.auth.presentation.forgot_password

import com.example.community_app.core.presentation.helpers.UiText

data class ForgotPasswordState(
  val email: String = "",
  val isLoading: Boolean = false,
  val errorMessage: UiText? = null,
  val showSuccessDialog: Boolean = false
)