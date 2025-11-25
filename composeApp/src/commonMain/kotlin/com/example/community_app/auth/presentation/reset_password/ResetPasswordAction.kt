package com.example.community_app.auth.presentation.reset_password

sealed interface ResetPasswordAction {
  data class OnOtpChange(val otp: String) : ResetPasswordAction
  data class OnPasswordChange(val password: String) : ResetPasswordAction
  data class OnPasswordRepeatChange(val password: String) : ResetPasswordAction
  data object OnTogglePasswordVisibility : ResetPasswordAction
  data object OnSubmit : ResetPasswordAction
  data object OnNavigateBack : ResetPasswordAction
  data object OnSuccessConfirm : ResetPasswordAction
}