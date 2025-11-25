package com.example.community_app.auth.presentation.forgot_password

sealed interface ForgotPasswordAction {
  data class OnEmailChange(val email: String) : ForgotPasswordAction
  data object OnSubmitClick : ForgotPasswordAction
  data object OnNavigateBack : ForgotPasswordAction
  data object OnDialogDismiss : ForgotPasswordAction
}